package fr.adcoop.jeudutao.infra.web.ratelimit;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class RateLimiter {

    private final ConcurrentHashMap<String, Deque<Instant>> timestamps = new ConcurrentHashMap<>();

    public boolean isAllowed(String key, int maxRequests, Duration window) {
        var allowed = new AtomicBoolean(false);
        timestamps.compute(key, (k, deque) -> {
            if (deque == null) {
                deque = new ArrayDeque<>();
            }
            var now = Instant.now();
            var windowStart = now.minus(window);
            // Purge entries older than the window
            while (!deque.isEmpty() && deque.peekFirst().isBefore(windowStart)) {
                deque.pollFirst();
            }
            if (deque.size() < maxRequests) {
                deque.addLast(now);
                allowed.set(true);
            }
            return deque;
        });
        return allowed.get();
    }
}
