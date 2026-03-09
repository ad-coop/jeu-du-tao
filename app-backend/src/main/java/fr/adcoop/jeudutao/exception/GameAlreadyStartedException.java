package fr.adcoop.jeudutao.exception;

public class GameAlreadyStartedException extends RuntimeException {

    public GameAlreadyStartedException(String handle) {
        super("Game already started: " + handle);
    }
}
