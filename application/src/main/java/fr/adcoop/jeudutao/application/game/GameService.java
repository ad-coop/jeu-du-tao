package fr.adcoop.jeudutao.application.game;

import fr.adcoop.jeudutao.application.port.MagicLinkSender;
import fr.adcoop.jeudutao.application.port.PasswordEncoder;
import fr.adcoop.jeudutao.domain.game.Game;
import fr.adcoop.jeudutao.domain.game.GameState;
import fr.adcoop.jeudutao.domain.game.HandleGenerator;
import fr.adcoop.jeudutao.domain.game.Player;
import fr.adcoop.jeudutao.domain.game.PlayerRole;
import fr.adcoop.jeudutao.domain.game.exception.GameAlreadyStartedException;
import fr.adcoop.jeudutao.domain.game.exception.GameNotFoundException;
import fr.adcoop.jeudutao.domain.game.exception.InvalidMagicLinkException;
import fr.adcoop.jeudutao.domain.game.exception.InvalidPasswordException;
import fr.adcoop.jeudutao.domain.game.exception.PlayerNotFoundException;
import fr.adcoop.jeudutao.domain.game.exception.UnauthorizedKickException;
import fr.adcoop.jeudutao.domain.game.port.GameRepository;
import fr.adcoop.jeudutao.domain.game.port.PlayerRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

public class GameService {

    public record CreateGameResult(Game game, Player guardian) {
    }

    public record JoinGameResult(Player player) {
    }

    public record RestoreResult(String playerId, String playerName) {
    }

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final HandleGenerator handleGenerator;
    private final PasswordEncoder passwordEncoder;
    private final MagicLinkSender magicLinkSender;

    public GameService(
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            HandleGenerator handleGenerator,
            PasswordEncoder passwordEncoder,
            MagicLinkSender magicLinkSender
    ) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.handleGenerator = handleGenerator;
        this.passwordEncoder = passwordEncoder;
        this.magicLinkSender = magicLinkSender;
    }

    public CreateGameResult createGame(String userName, String email, String password) {
        var handle = handleGenerator.generate(h -> !gameRepository.existsByHandle(h));
        var passwordHash = password != null ? passwordEncoder.encode(password) : null;
        var guardianId = UUID.randomUUID().toString();

        String magicLinkToken = null;
        Instant magicLinkExpiry = null;
        if (email != null) {
            var tokenBytes = new byte[16];
            new SecureRandom().nextBytes(tokenBytes);
            magicLinkToken = HexFormat.of().formatHex(tokenBytes);
            magicLinkExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
        }

        var game = new Game(
                handle,
                passwordHash,
                Instant.now(),
                GameState.WAITING,
                guardianId,
                userName,
                magicLinkToken,
                magicLinkExpiry,
                email
        );
        gameRepository.save(game);

        var guardian = new Player(guardianId, userName, PlayerRole.GUARDIAN, handle);
        playerRepository.save(guardian);

        if (email != null) {
            magicLinkSender.sendLink(email, handle, magicLinkToken);
        }

        return new CreateGameResult(game, guardian);
    }

    public JoinGameResult joinGame(String handle, String userName, String password) {
        var maybeGame = gameRepository.findByHandle(handle);
        if (maybeGame.isEmpty()) {
            throw new GameNotFoundException(handle);
        }
        var game = maybeGame.get();

        if (game.state() == GameState.STARTED) {
            throw new GameAlreadyStartedException(handle);
        }

        if (game.passwordHash() != null) {
            if (password == null || !passwordEncoder.matches(password, game.passwordHash())) {
                throw new InvalidPasswordException();
            }
        }

        var player = new Player(UUID.randomUUID().toString(), userName, PlayerRole.PLAYER, handle);
        playerRepository.save(player);

        return new JoinGameResult(player);
    }

    public void kickPlayer(String handle, String requestingPlayerId, String targetPlayerId) {
        var maybeRequester = playerRepository.findById(requestingPlayerId);
        if (maybeRequester.isEmpty()) {
            throw new PlayerNotFoundException(requestingPlayerId);
        }
        var requester = maybeRequester.get();

        if (requester.role() != PlayerRole.GUARDIAN || !requester.gameHandle().equals(handle)) {
            throw new UnauthorizedKickException(requestingPlayerId);
        }

        playerRepository.deleteById(targetPlayerId);
    }

    public void removePlayer(String playerId) {
        playerRepository.deleteById(playerId);
    }

    public List<Player> getPlayers(String handle) {
        if (!gameRepository.existsByHandle(handle)) {
            throw new GameNotFoundException(handle);
        }
        return playerRepository.findByGameHandle(handle);
    }

    public Game getGameInfo(String handle) {
        return gameRepository.findByHandle(handle)
                .orElseThrow(() -> new GameNotFoundException(handle));
    }

    public RestoreResult restoreGuardian(String handle, String token) {
        var game = gameRepository.findByHandle(handle)
                .orElseThrow(() -> new GameNotFoundException(handle));

        if (game.magicLinkToken() == null
                || !MessageDigest.isEqual(
                        game.magicLinkToken().getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8))
                || !Instant.now().isBefore(game.magicLinkExpiry())) {
            throw new InvalidMagicLinkException();
        }

        var guardian = playerRepository.findById(game.guardianId())
                .orElseGet(() -> {
                    var recreated = new Player(game.guardianId(), game.guardianName(), PlayerRole.GUARDIAN, handle);
                    playerRepository.save(recreated);
                    return recreated;
                });

        return new RestoreResult(guardian.id(), guardian.name());
    }
}
