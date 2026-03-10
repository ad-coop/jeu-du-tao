package fr.adcoop.jeudutao.api.game;

import fr.adcoop.jeudutao.domain.game.Game;
import fr.adcoop.jeudutao.domain.game.GameState;
import fr.adcoop.jeudutao.domain.game.Player;
import fr.adcoop.jeudutao.domain.game.PlayerRole;
import fr.adcoop.jeudutao.domain.game.exception.InvalidMagicLinkException;
import fr.adcoop.jeudutao.application.game.GameService;
import fr.adcoop.jeudutao.service.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GameControllerTest {

    private MockMvc mockMvc;
    private GameService gameService;
    private SimpMessagingTemplate messagingTemplate;
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        rateLimiter = mock(RateLimiter.class);
        reset(gameService, messagingTemplate, rateLimiter);

        when(rateLimiter.isAllowed(anyString(), anyInt(), any())).thenReturn(true);

        var controller = new GameController(gameService, messagingTemplate, rateLimiter);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GameExceptionHandler())
                .build();
    }

    @Test
    void createGame_withValidRequest_returns201WithGameDetails() throws Exception {
        var game = new Game("GAME01", null, Instant.now(), GameState.WAITING, "guardian-id", "Alice", null, null, null);
        var guardian = new Player("guardian-id", "Alice", PlayerRole.GUARDIAN, "GAME01");
        when(gameService.createGame("Alice", null, null)).thenReturn(new GameService.CreateGameResult(game, guardian));
        when(gameService.getPlayers("GAME01")).thenReturn(List.of(guardian));

        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": null, "password": null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.handle").value("GAME01"))
                .andExpect(jsonPath("$.playerId").value("guardian-id"))
                .andExpect(jsonPath("$.passwordProtected").value(false))
                .andExpect(jsonPath("$.hasEmail").value(false));
    }

    @Test
    void createGame_withEmail_returnsHasEmailTrue() throws Exception {
        var game = new Game("GAME01", null, Instant.now(), GameState.WAITING, "guardian-id", "Alice", null, null, "alice@example.com");
        var guardian = new Player("guardian-id", "Alice", PlayerRole.GUARDIAN, "GAME01");
        when(gameService.createGame("Alice", "alice@example.com", null)).thenReturn(new GameService.CreateGameResult(game, guardian));
        when(gameService.getPlayers("GAME01")).thenReturn(List.of(guardian));

        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": "alice@example.com", "password": null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasEmail").value(true));
    }

    @Test
    void createGame_withBlankUserName_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "  ", "email": null, "password": null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation.error"));
    }

    @Test
    void createGame_withUserNameTooLong_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("{\"userName\": \"" + "A".repeat(51) + "\", \"email\": null, \"password\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGame_withInvalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": "not-an-email", "password": null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGameInfo_withValidHandle_returns200() throws Exception {
        var game = new Game("GAME01", null, Instant.now(), GameState.WAITING, "guardian-id", "Alice", null, null, null);
        when(gameService.getGameInfo("GAME01")).thenReturn(game);

        mockMvc.perform(get("/api/games/GAME01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value("GAME01"))
                .andExpect(jsonPath("$.state").value("WAITING"))
                .andExpect(jsonPath("$.passwordProtected").value(false));
    }

    @Test
    void getGameInfo_withInvalidHandleFormat_returns404() throws Exception {
        mockMvc.perform(get("/api/games/invalid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void joinGame_withValidRequest_returns201() throws Exception {
        var player = new Player("player-id", "Bob", PlayerRole.PLAYER, "GAME01");
        when(gameService.joinGame("GAME01", "Bob", null)).thenReturn(new GameService.JoinGameResult(player));
        when(gameService.getPlayers("GAME01")).thenReturn(List.of(player));

        mockMvc.perform(post("/api/games/GAME01/players")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Bob", "password": null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerId").value("player-id"));
    }

    @Test
    void getPlayers_returnsPlayerList() throws Exception {
        var players = List.of(
                new Player("p1", "Alice", PlayerRole.GUARDIAN, "GAME01"),
                new Player("p2", "Bob", PlayerRole.PLAYER, "GAME01")
        );
        when(gameService.getPlayers("GAME01")).thenReturn(players);

        mockMvc.perform(get("/api/games/GAME01/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void getPlayers_returnsLowercaseRoles() throws Exception {
        var players = List.of(
                new Player("p1", "Alice", PlayerRole.GUARDIAN, "GAME01"),
                new Player("p2", "Bob", PlayerRole.PLAYER, "GAME01")
        );
        when(gameService.getPlayers("GAME01")).thenReturn(players);

        mockMvc.perform(get("/api/games/GAME01/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("guardian"))
                .andExpect(jsonPath("$[1].role").value("player"));
    }

    @Test
    void kickPlayer_withValidRequest_returns204() throws Exception {
        when(gameService.getPlayers("GAME01")).thenReturn(List.of());

        mockMvc.perform(delete("/api/games/GAME01/players/player-id")
                        .header("X-Player-Id", "guardian-id"))
                .andExpect(status().isNoContent());

        verify(gameService).kickPlayer("GAME01", "guardian-id", "player-id");
    }

    @Test
    void restoreGame_returns200WithGuardianInfo() throws Exception {
        var restoreResult = new GameService.RestoreResult("guardian-id", "Alice");
        var game = new Game("GAME01", null, Instant.now(), GameState.WAITING, "guardian-id", "Alice", null, null, "alice@example.com");
        when(gameService.restoreGuardian("GAME01", "valid-token")).thenReturn(restoreResult);
        when(gameService.getGameInfo("GAME01")).thenReturn(game);
        when(gameService.getPlayers("GAME01")).thenReturn(List.of());

        mockMvc.perform(post("/api/games/GAME01/restore")
                        .contentType("application/json")
                        .content("""
                                {"token": "valid-token"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.playerId").value("guardian-id"))
                .andExpect(jsonPath("$.playerName").value("Alice"))
                .andExpect(jsonPath("$.passwordProtected").value(false))
                .andExpect(jsonPath("$.hasEmail").value(true));
    }

    @Test
    void restoreGame_whenInvalidToken_returns401() throws Exception {
        when(gameService.restoreGuardian("GAME01", "bad-token")).thenThrow(new InvalidMagicLinkException());

        mockMvc.perform(post("/api/games/GAME01/restore")
                        .contentType("application/json")
                        .content("""
                                {"token": "bad-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("game.invalidMagicLink"));
    }
}
