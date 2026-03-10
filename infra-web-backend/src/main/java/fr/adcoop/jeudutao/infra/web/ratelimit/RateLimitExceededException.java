package fr.adcoop.jeudutao.infra.web.ratelimit;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String key) {
        super("Rate limit exceeded for: " + key);
    }
}
