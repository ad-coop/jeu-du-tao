package fr.adcoop.jeudutao.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String key) {
        super("Rate limit exceeded for: " + key);
    }
}
