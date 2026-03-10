package fr.adcoop.jeudutao.domain.game.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String handle) {
        super("Game not found: " + handle);
    }
}
