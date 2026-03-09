package fr.adcoop.jeudutao.exception;

public class UnauthorizedKickException extends RuntimeException {

    public UnauthorizedKickException(String playerId) {
        super("Player is not authorized to kick: " + playerId);
    }
}
