package fr.adcoop.jeudutao.application.game.command;

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
import fr.adcoop.jeudutao.domain.game.port.GameCommandRepository;
import fr.adcoop.jeudutao.domain.game.port.PlayerCommandRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

public class GameCommandService {

    public record CreateGameResult(String handle, String guardianId, boolean passwordProtected, boolean hasEmail) {}

    public record JoinGameResult(String playerId) {}

    public record RemovePlayerResult(String gameHandle) {}

    public record RestoreResult(String playerId, String playerName) {}

    private final GameCommandRepository gameCommandRepository;
    private final PlayerCommandRepository playerCommandRepository;
    private final HandleGenerator handleGenerator;
    private final PasswordEncoder passwordEncoder;
    private final MagicLinkSender magicLinkSender;

    public GameCommandService(
            GameCommandRepository gameCommandRepository,
            PlayerCommandRepository playerCommandRepository,
            HandleGenerator handleGenerator,
            PasswordEncoder passwordEncoder,
            MagicLinkSender magicLinkSender
    ) {
        this.gameCommandRepository = gameCommandRepository;
        this.playerCommandRepository = playerCommandRepository;
        this.handleGenerator = handleGenerator;
        this.passwordEncoder = passwordEncoder;
        this.magicLinkSender = magicLinkSender;
    }

    public CreateGameResult createGame(String userName, String email, String password) {
        var handle = handleGenerator.generate(h -> !gameCommandRepository.existsByHandle(h));
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
        gameCommandRepository.save(game);

        var guardian = new Player(guardianId, userName, PlayerRole.GUARDIAN, handle);
        playerCommandRepository.save(guardian);

        if (email != null) {
            magicLinkSender.sendLink(email, handle, magicLinkToken);
        }

        return new CreateGameResult(handle, guardianId, passwordHash != null, email != null);
    }

    public JoinGameResult joinGame(String handle, String userName, String password) {
        var maybeGame = gameCommandRepository.findByHandle(handle);
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

        var playerId = UUID.randomUUID().toString();
        var player = new Player(playerId, userName, PlayerRole.PLAYER, handle);
        playerCommandRepository.save(player);

        return new JoinGameResult(playerId);
    }

    public void kickPlayer(String handle, String requestingPlayerId, String targetPlayerId) {
        var maybeRequester = playerCommandRepository.findById(requestingPlayerId);
        if (maybeRequester.isEmpty()) {
            throw new PlayerNotFoundException(requestingPlayerId);
        }
        var requester = maybeRequester.get();

        if (requester.role() != PlayerRole.GUARDIAN || !requester.gameHandle().equals(handle)) {
            throw new UnauthorizedKickException(requestingPlayerId);
        }

        playerCommandRepository.deleteById(targetPlayerId);
    }

    public Optional<RemovePlayerResult> removePlayer(String playerId) {
        var maybePlayer = playerCommandRepository.findById(playerId);
        playerCommandRepository.deleteById(playerId);
        return maybePlayer.map(p -> new RemovePlayerResult(p.gameHandle()));
    }

    public RestoreResult restoreGuardian(String handle, String token) {
        var game = gameCommandRepository.findByHandle(handle)
                .orElseThrow(() -> new GameNotFoundException(handle));

        if (game.magicLinkToken() == null
                || !MessageDigest.isEqual(
                        game.magicLinkToken().getBytes(StandardCharsets.UTF_8),
                        token.getBytes(StandardCharsets.UTF_8))
                || !Instant.now().isBefore(game.magicLinkExpiry())) {
            throw new InvalidMagicLinkException();
        }

        var guardian = playerCommandRepository.findById(game.guardianId())
                .orElseGet(() -> {
                    var recreated = new Player(game.guardianId(), game.guardianName(), PlayerRole.GUARDIAN, handle);
                    playerCommandRepository.save(recreated);
                    return recreated;
                });

        return new RestoreResult(guardian.id(), guardian.name());
    }
}
