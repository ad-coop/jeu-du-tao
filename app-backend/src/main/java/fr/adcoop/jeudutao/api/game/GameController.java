package fr.adcoop.jeudutao.api.game;

import fr.adcoop.jeudutao.exception.GameNotFoundException;
import fr.adcoop.jeudutao.exception.RateLimitExceededException;
import fr.adcoop.jeudutao.service.GameService;
import fr.adcoop.jeudutao.service.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final Pattern HANDLE_PATTERN = Pattern.compile("[A-Z0-9]{6}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RateLimiter rateLimiter;

    public GameController(GameService gameService, SimpMessagingTemplate messagingTemplate, RateLimiter rateLimiter) {
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGameResponse createGame(@RequestBody CreateGameRequest request, HttpServletRequest httpRequest) {
        var ip = httpRequest.getRemoteAddr();
        if (!rateLimiter.isAllowed("create:" + ip, 10, Duration.ofHours(1))) {
            throw new RateLimitExceededException("create:" + ip);
        }

        validateUserName(request.userName());
        if (request.email() != null) {
            validateEmail(request.email());
        }
        if (request.password() != null) {
            validatePassword(request.password());
        }

        var result = gameService.createGame(
                request.userName().trim(),
                request.email(),
                request.password()
        );

        broadcastPlayerList(result.game().handle());

        return new CreateGameResponse(
                result.game().handle(),
                result.guardian().id(),
                result.game().passwordHash() != null,
                result.game().email() != null
        );
    }

    @GetMapping("/{handle}")
    public GameInfoResponse getGameInfo(@PathVariable("handle") String handle) {
        validateHandle(handle);
        var game = gameService.getGameInfo(handle);
        return new GameInfoResponse(game.handle(), game.state().name(), game.passwordHash() != null);
    }

    @PostMapping("/{handle}/players")
    @ResponseStatus(HttpStatus.CREATED)
    public JoinGameResponse joinGame(
            @PathVariable("handle") String handle,
            @RequestBody JoinGameRequest request
    ) {
        validateHandle(handle);
        validateUserName(request.userName());

        if (request.password() != null) {
            if (!rateLimiter.isAllowed("password:" + handle, 5, Duration.ofMinutes(1))) {
                throw new RateLimitExceededException("password:" + handle);
            }
        }

        var result = gameService.joinGame(handle, request.userName().trim(), request.password());
        broadcastPlayerList(handle);

        return new JoinGameResponse(result.player().id());
    }

    @GetMapping("/{handle}/players")
    public List<PlayerInfo> getPlayers(@PathVariable("handle") String handle) {
        validateHandle(handle);
        return gameService.getPlayers(handle).stream()
                .map(PlayerInfo::from)
                .toList();
    }

    @PostMapping("/{handle}/restore")
    public RestoreGameResponse restoreGame(
            @PathVariable("handle") String handle,
            @RequestBody RestoreGameRequest request
    ) {
        validateHandle(handle);
        var result = gameService.restoreGuardian(handle, request.token());
        broadcastPlayerList(handle);
        var game = gameService.getGameInfo(handle);
        return new RestoreGameResponse(result.playerId(), result.playerName(), game.passwordHash() != null, game.email() != null);
    }

    @DeleteMapping("/{handle}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickPlayer(
            @PathVariable("handle") String handle,
            @PathVariable("playerId") String playerId,
            @RequestHeader("X-Player-Id") String requestingPlayerId
    ) {
        validateHandle(handle);
        gameService.kickPlayer(handle, requestingPlayerId, playerId);
        broadcastPlayerList(handle);
        messagingTemplate.convertAndSend("/topic/games/" + handle + "/kick/" + playerId, (Object) Map.of());
    }

    private void validateHandle(String handle) {
        if (handle == null || !HANDLE_PATTERN.matcher(handle).matches()) {
            throw new GameNotFoundException(handle);
        }
    }

    private void validateUserName(String userName) {
        if (userName == null || userName.trim().isBlank()) {
            throw new IllegalArgumentException("User name is required");
        }
        if (userName.trim().length() > 50) {
            throw new IllegalArgumentException("User name cannot exceed 50 characters");
        }
    }

    private void validateEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }

    private void validatePassword(String password) {
        if (password.length() > 100) {
            throw new IllegalArgumentException("Password cannot exceed 100 characters");
        }
    }

    private void broadcastPlayerList(String handle) {
        var players = gameService.getPlayers(handle).stream()
                .map(PlayerInfo::from)
                .toList();
        messagingTemplate.convertAndSend("/topic/games/" + handle + "/players", players);
    }
}
