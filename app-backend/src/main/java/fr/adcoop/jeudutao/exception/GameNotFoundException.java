package fr.adcoop.jeudutao.exception;

public class GameNotFoundException extends RuntimeException {

    public GameNotFoundException(String handle) {
        super("Game not found: " + handle);
    }
}
