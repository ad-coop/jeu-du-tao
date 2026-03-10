package fr.adcoop.jeudutao.domain.game.exception;

public class InvalidMagicLinkException extends RuntimeException {
    public InvalidMagicLinkException() {
        super("Invalid or expired magic link");
    }
}
