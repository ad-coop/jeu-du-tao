package fr.adcoop.jeudutao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimiterTest {

    private RateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new RateLimiter();
    }

    @Test
    void isAllowed_underLimit_returnsTrue() {
        var isAllowed = rateLimiter.isAllowed("key1", 5, Duration.ofMinutes(1));

        assertThat(isAllowed).isTrue();
    }

    @Test
    void isAllowed_atLimit_returnsTrue() {
        for (int i = 0; i < 4; i++) {
            rateLimiter.isAllowed("key2", 5, Duration.ofMinutes(1));
        }

        assertThat(rateLimiter.isAllowed("key2", 5, Duration.ofMinutes(1))).isTrue();
    }

    @Test
    void isAllowed_overLimit_returnsFalse() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed("key3", 5, Duration.ofMinutes(1));
        }

        assertThat(rateLimiter.isAllowed("key3", 5, Duration.ofMinutes(1))).isFalse();
    }

    @Test
    void isAllowed_differentKeys_areIndependent() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.isAllowed("keyA", 5, Duration.ofMinutes(1));
        }

        assertThat(rateLimiter.isAllowed("keyB", 5, Duration.ofMinutes(1))).isTrue();
    }
}
