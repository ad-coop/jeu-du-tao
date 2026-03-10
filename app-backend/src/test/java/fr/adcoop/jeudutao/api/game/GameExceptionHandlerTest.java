package fr.adcoop.jeudutao.api.game;

import fr.adcoop.jeudutao.domain.game.exception.GameAlreadyStartedException;
import fr.adcoop.jeudutao.domain.game.exception.GameNotFoundException;
import fr.adcoop.jeudutao.domain.game.exception.InvalidPasswordException;
import fr.adcoop.jeudutao.domain.game.exception.PlayerNotFoundException;
import fr.adcoop.jeudutao.exception.RateLimitExceededException;
import fr.adcoop.jeudutao.domain.game.exception.UnauthorizedKickException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GameExceptionHandlerTest {

    private GameExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GameExceptionHandler();
    }

    @Test
    void handleGameNotFound_returns404ErrorResponse() {
        var response = handler.handleGameNotFound(new GameNotFoundException("GAME01"));

        assertThat(response.error()).isEqualTo("game.notFound");
    }

    @Test
    void handleInvalidPassword_returns401ErrorResponse() {
        var response = handler.handleInvalidPassword(new InvalidPasswordException());

        assertThat(response.error()).isEqualTo("game.invalidPassword");
    }

    @Test
    void handleGameAlreadyStarted_returns409ErrorResponse() {
        var response = handler.handleGameAlreadyStarted(new GameAlreadyStartedException("GAME01"));

        assertThat(response.error()).isEqualTo("game.alreadyStarted");
    }

    @Test
    void handleUnauthorizedKick_returns403ErrorResponse() {
        var response = handler.handleUnauthorizedKick(new UnauthorizedKickException("player-id"));

        assertThat(response.error()).isEqualTo("game.kickUnauthorized");
    }

    @Test
    void handleRateLimitExceeded_returns429ErrorResponse() {
        var response = handler.handleRateLimitExceeded(new RateLimitExceededException("key"));

        assertThat(response.error()).isEqualTo("rateLimit.exceeded");
    }

    @Test
    void handlePlayerNotFound_returns404ErrorResponse() {
        var response = handler.handlePlayerNotFound(new PlayerNotFoundException("player-id"));

        assertThat(response.error()).isEqualTo("player.notFound");
    }

    @Test
    void handleIllegalArgument_returns400ErrorResponse() {
        var response = handler.handleIllegalArgument(new IllegalArgumentException("bad input"));

        assertThat(response.error()).isEqualTo("validation.error");
        assertThat(response.message()).isEqualTo("bad input");
    }
}
