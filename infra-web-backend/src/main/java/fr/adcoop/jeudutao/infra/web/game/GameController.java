package fr.adcoop.jeudutao.infra.web.game;

import fr.adcoop.jeudutao.application.game.command.GameCommandService;
import fr.adcoop.jeudutao.application.game.query.GameQueryService;
import fr.adcoop.jeudutao.domain.game.exception.GameNotFoundException;
import fr.adcoop.jeudutao.infra.web.ratelimit.RateLimitExceededException;
import fr.adcoop.jeudutao.infra.web.ratelimit.RateLimiter;
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

    private final GameCommandService commandService;
    private final GameQueryService queryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final RateLimiter rateLimiter;

    public GameController(GameCommandService commandService, GameQueryService queryService, SimpMessagingTemplate messagingTemplate, RateLimiter rateLimiter) {
        this.commandService = commandService;
        this.queryService = queryService;
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

        var result = commandService.createGame(request.userName().trim(), request.email(), request.password());
        broadcastPlayerList(result.handle());

        return new CreateGameResponse(result.handle(), result.guardianId(), result.passwordProtected(), result.hasEmail());
    }

    @GetMapping("/{handle}")
    public GameInfoResponse getGameInfo(@PathVariable("handle") String handle) {
        validateHandle(handle);
        var view = queryService.getGameInfo(handle);
        return new GameInfoResponse(view.handle(), view.state(), view.passwordProtected());
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

        var result = commandService.joinGame(handle, request.userName().trim(), request.password());
        broadcastPlayerList(handle);

        return new JoinGameResponse(result.playerId());
    }

    @GetMapping("/{handle}/players")
    public List<PlayerInfo> getPlayers(@PathVariable("handle") String handle) {
        validateHandle(handle);
        return queryService.getPlayers(handle).stream()
                .map(PlayerInfo::from)
                .toList();
    }

    @PostMapping("/{handle}/restore")
    public RestoreGameResponse restoreGame(
            @PathVariable("handle") String handle,
            @RequestBody RestoreGameRequest request
    ) {
        validateHandle(handle);
        var result = commandService.restoreGuardian(handle, request.token());
        broadcastPlayerList(handle);
        // Read the game state after restore to get passwordProtected/hasEmail for the response.
        // Synchronous CQRS: command and query share the same DB transaction, so this read is consistent.
        var view = queryService.getGameInfo(handle);
        return new RestoreGameResponse(result.playerId(), result.playerName(), view.passwordProtected(), view.hasEmail());
    }

    @DeleteMapping("/{handle}/players/{playerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kickPlayer(
            @PathVariable("handle") String handle,
            @PathVariable("playerId") String playerId,
            @RequestHeader("X-Player-Id") String requestingPlayerId
    ) {
        validateHandle(handle);
        commandService.kickPlayer(handle, requestingPlayerId, playerId);
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
        var players = queryService.getPlayers(handle).stream()
                .map(PlayerInfo::from)
                .toList();
        messagingTemplate.convertAndSend("/topic/games/" + handle + "/players", players);
    }
}
