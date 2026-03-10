package fr.adcoop.jeudutao.exception;

public class InvalidMagicLinkException extends RuntimeException {

    public InvalidMagicLinkException() {
        super("Invalid or expired magic link");
    }
}
