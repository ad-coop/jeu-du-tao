package fr.adcoop.jeudutao.infra.web.game;

import fr.adcoop.jeudutao.application.game.command.GameCommandService;
import fr.adcoop.jeudutao.application.game.query.GameInfoView;
import fr.adcoop.jeudutao.application.game.query.GameQueryService;
import fr.adcoop.jeudutao.application.game.query.PlayerView;
import fr.adcoop.jeudutao.domain.game.exception.InvalidMagicLinkException;
import fr.adcoop.jeudutao.infra.web.ratelimit.RateLimiter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    private GameCommandService commandService;
    private GameQueryService queryService;
    private SimpMessagingTemplate messagingTemplate;
    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        commandService = mock(GameCommandService.class);
        queryService = mock(GameQueryService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        rateLimiter = mock(RateLimiter.class);
        reset(commandService, queryService, messagingTemplate, rateLimiter);

        when(rateLimiter.isAllowed(anyString(), anyInt(), any())).thenReturn(true);

        var controller = new GameController(commandService, queryService, messagingTemplate, rateLimiter);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GameExceptionHandler())
                .build();
    }

    @Test
    void createGame_whenValidRequest_returns201WithGameDetails() throws Exception {
        when(commandService.createGame("Alice", null, null))
                .thenReturn(new GameCommandService.CreateGameResult("GAME01", "guardian-id", false, false));
        when(queryService.getPlayers("GAME01")).thenReturn(List.of(new PlayerView("guardian-id", "Alice", "guardian")));

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
    void createGame_whenEmail_returnsHasEmailTrue() throws Exception {
        when(commandService.createGame("Alice", "alice@example.com", null))
                .thenReturn(new GameCommandService.CreateGameResult("GAME01", "guardian-id", false, true));
        when(queryService.getPlayers("GAME01")).thenReturn(List.of(new PlayerView("guardian-id", "Alice", "guardian")));

        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": "alice@example.com", "password": null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasEmail").value(true));
    }

    @Test
    void createGame_whenBlankUserName_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "  ", "email": null, "password": null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation.error"));
    }

    @Test
    void createGame_whenUserNameTooLong_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("{\"userName\": \"" + "A".repeat(51) + "\", \"email\": null, \"password\": null}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createGame_whenInvalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": "not-an-email", "password": null}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getGameInfo_whenValidHandle_returns200() throws Exception {
        when(queryService.getGameInfo("GAME01"))
                .thenReturn(new GameInfoView("GAME01", "WAITING", false, false));

        mockMvc.perform(get("/api/games/GAME01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.handle").value("GAME01"))
                .andExpect(jsonPath("$.state").value("WAITING"))
                .andExpect(jsonPath("$.passwordProtected").value(false));
    }

    @Test
    void getGameInfo_whenInvalidHandleFormat_returns404() throws Exception {
        mockMvc.perform(get("/api/games/invalid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void joinGame_whenValidRequest_returns201() throws Exception {
        when(commandService.joinGame("GAME01", "Bob", null))
                .thenReturn(new GameCommandService.JoinGameResult("player-id"));
        when(queryService.getPlayers("GAME01")).thenReturn(List.of(new PlayerView("player-id", "Bob", "player")));

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
        when(queryService.getPlayers("GAME01")).thenReturn(List.of(
                new PlayerView("p1", "Alice", "guardian"),
                new PlayerView("p2", "Bob", "player")
        ));

        mockMvc.perform(get("/api/games/GAME01/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice"))
                .andExpect(jsonPath("$[1].name").value("Bob"));
    }

    @Test
    void getPlayers_returnsLowercaseRoles() throws Exception {
        when(queryService.getPlayers("GAME01")).thenReturn(List.of(
                new PlayerView("p1", "Alice", "guardian"),
                new PlayerView("p2", "Bob", "player")
        ));

        mockMvc.perform(get("/api/games/GAME01/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("guardian"))
                .andExpect(jsonPath("$[1].role").value("player"));
    }

    @Test
    void kickPlayer_whenValidRequest_returns204() throws Exception {
        when(queryService.getPlayers("GAME01")).thenReturn(List.of());

        mockMvc.perform(delete("/api/games/GAME01/players/player-id")
                        .header("X-Player-Id", "guardian-id"))
                .andExpect(status().isNoContent());

        verify(commandService).kickPlayer("GAME01", "guardian-id", "player-id");
    }

    @Test
    void restoreGame_returns200WithGuardianInfo() throws Exception {
        when(commandService.restoreGuardian("GAME01", "valid-token"))
                .thenReturn(new GameCommandService.RestoreResult("guardian-id", "Alice"));
        when(queryService.getGameInfo("GAME01"))
                .thenReturn(new GameInfoView("GAME01", "WAITING", false, true));
        when(queryService.getPlayers("GAME01")).thenReturn(List.of());

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
        when(commandService.restoreGuardian("GAME01", "bad-token")).thenThrow(new InvalidMagicLinkException());

        mockMvc.perform(post("/api/games/GAME01/restore")
                        .contentType("application/json")
                        .content("""
                                {"token": "bad-token"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("game.invalidMagicLink"));
    }
}
