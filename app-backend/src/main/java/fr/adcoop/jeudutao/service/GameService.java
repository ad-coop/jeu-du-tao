package fr.adcoop.jeudutao.service;

import fr.adcoop.jeudutao.domain.Game;
import fr.adcoop.jeudutao.domain.GameState;
import fr.adcoop.jeudutao.domain.Player;
import fr.adcoop.jeudutao.domain.PlayerRole;
import fr.adcoop.jeudutao.exception.GameAlreadyStartedException;
import fr.adcoop.jeudutao.exception.GameNotFoundException;
import fr.adcoop.jeudutao.exception.InvalidPasswordException;
import fr.adcoop.jeudutao.exception.PlayerNotFoundException;
import fr.adcoop.jeudutao.exception.UnauthorizedKickException;
import fr.adcoop.jeudutao.repository.GameRepository;
import fr.adcoop.jeudutao.repository.PlayerRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    public record CreateGameResult(Game game, Player guardian) {
    }

    public record JoinGameResult(Player player) {
    }

    private final GameRepository gameRepository;
    private final PlayerRepository playerRepository;
    private final HandleGenerator handleGenerator;
    private final PasswordService passwordService;
    private final MagicLinkService magicLinkService;

    public GameService(
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            HandleGenerator handleGenerator,
            PasswordService passwordService,
            MagicLinkService magicLinkService
    ) {
        this.gameRepository = gameRepository;
        this.playerRepository = playerRepository;
        this.handleGenerator = handleGenerator;
        this.passwordService = passwordService;
        this.magicLinkService = magicLinkService;
    }

    public CreateGameResult createGame(String userName, String email, String password) {
        var handle = handleGenerator.generate(h -> !gameRepository.existsByHandle(h));
        var passwordHash = password != null ? passwordService.encode(password) : null;
        var guardianId = UUID.randomUUID().toString();

        String magicLinkToken = null;
        Instant magicLinkExpiry = null;
        if (email != null) {
            magicLinkToken = magicLinkService.generateToken();
            magicLinkExpiry = Instant.now().plus(7, ChronoUnit.DAYS);
        }

        var game = new Game(
                handle,
                passwordHash,
                Instant.now(),
                GameState.WAITING,
                guardianId,
                magicLinkToken,
                magicLinkExpiry,
                email
        );
        gameRepository.save(game);

        var guardian = new Player(guardianId, userName, PlayerRole.GUARDIAN, handle);
        playerRepository.save(guardian);

        if (email != null) {
            magicLinkService.sendLink(email, handle, magicLinkToken);
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
            if (password == null || !passwordService.matches(password, game.passwordHash())) {
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
}
