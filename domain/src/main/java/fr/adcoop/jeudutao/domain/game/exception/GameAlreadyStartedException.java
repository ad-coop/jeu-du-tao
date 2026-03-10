package fr.adcoop.jeudutao.domain.game.exception;

public class GameAlreadyStartedException extends RuntimeException {
    public GameAlreadyStartedException(String handle) {
        super("Game already started: " + handle);
    }
}
