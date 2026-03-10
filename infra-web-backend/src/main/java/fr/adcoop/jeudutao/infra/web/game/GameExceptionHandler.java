package fr.adcoop.jeudutao.infra.web.game;

import fr.adcoop.jeudutao.domain.game.exception.GameAlreadyStartedException;
import fr.adcoop.jeudutao.domain.game.exception.GameNotFoundException;
import fr.adcoop.jeudutao.domain.game.exception.InvalidMagicLinkException;
import fr.adcoop.jeudutao.domain.game.exception.InvalidPasswordException;
import fr.adcoop.jeudutao.domain.game.exception.PlayerNotFoundException;
import fr.adcoop.jeudutao.infra.web.ratelimit.RateLimitExceededException;
import fr.adcoop.jeudutao.domain.game.exception.UnauthorizedKickException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GameExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleGameNotFound(GameNotFoundException ex) {
        return new ErrorResponse("game.notFound", ex.getMessage());
    }

    @ExceptionHandler(InvalidPasswordException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidPassword(InvalidPasswordException ex) {
        return new ErrorResponse("game.invalidPassword", ex.getMessage());
    }

    @ExceptionHandler(InvalidMagicLinkException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleInvalidMagicLink(InvalidMagicLinkException ex) {
        return new ErrorResponse("game.invalidMagicLink", ex.getMessage());
    }

    @ExceptionHandler(GameAlreadyStartedException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleGameAlreadyStarted(GameAlreadyStartedException ex) {
        return new ErrorResponse("game.alreadyStarted", ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedKickException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleUnauthorizedKick(UnauthorizedKickException ex) {
        return new ErrorResponse("game.kickUnauthorized", ex.getMessage());
    }

    @ExceptionHandler(RateLimitExceededException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ErrorResponse handleRateLimitExceeded(RateLimitExceededException ex) {
        return new ErrorResponse("rateLimit.exceeded", ex.getMessage());
    }

    @ExceptionHandler(PlayerNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handlePlayerNotFound(PlayerNotFoundException ex) {
        return new ErrorResponse("player.notFound", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        return new ErrorResponse("validation.error", ex.getMessage());
    }
}
