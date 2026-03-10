package fr.adcoop.jeudutao.api.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class GameControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createGame_withValidRequest_returns201() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": null, "password": null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.handle").exists())
                .andExpect(jsonPath("$.playerId").exists())
                .andExpect(jsonPath("$.passwordProtected").value(false))
                .andExpect(jsonPath("$.hasEmail").value(false));
    }

    @Test
    void createGame_withEmail_returnsHasEmailTrue() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": "alice@example.com", "password": null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.hasEmail").value(true));
    }

    @Test
    void createGame_withPassword_returnsPasswordProtectedTrue() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": null, "password": "secret123"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.passwordProtected").value(true));
    }

    @Test
    void getGameInfo_withUnknownHandle_returns404() throws Exception {
        mockMvc.perform(get("/api/games/XXXXXX"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getGameInfo_withInvalidHandleFormat_returns404() throws Exception {
        mockMvc.perform(get("/api/games/invalid"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createGameAndJoin_fullFlow() throws Exception {
        var createResult = mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": null, "password": null}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        var responseBody = createResult.getResponse().getContentAsString();
        var handle = responseBody.replaceAll(".*\"handle\":\"([A-Z0-9]{6})\".*", "$1");

        mockMvc.perform(post("/api/games/" + handle + "/players")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Bob", "password": null}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.playerId").exists());

        mockMvc.perform(get("/api/games/" + handle + "/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[1].name").exists());
    }

    @Test
    void createGame_withBlankUserName_returns400() throws Exception {
        mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "", "email": null, "password": null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation.error"));
    }

    @Test
    void joinGame_withWrongPassword_returns401() throws Exception {
        var createResult = mockMvc.perform(post("/api/games")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Alice", "email": null, "password": "correctpass"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        var responseBody = createResult.getResponse().getContentAsString();
        var handle = responseBody.replaceAll(".*\"handle\":\"([A-Z0-9]{6})\".*", "$1");

        mockMvc.perform(post("/api/games/" + handle + "/players")
                        .contentType("application/json")
                        .content("""
                                {"userName": "Bob", "password": "wrongpass"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("game.invalidPassword"));
    }
}
