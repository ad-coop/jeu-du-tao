package fr.adcoop.jeudutao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class HandleGeneratorTest {

    private HandleGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new HandleGenerator();
    }

    @Test
    void generate_producesSixCharAlphanumericString() {
        var handle = generator.generate(h -> true);

        assertThat(handle).hasSize(6).matches("[A-Z0-9]{6}");
    }

    @Test
    void generate_retriesOnCollision() {
        var callCount = new AtomicInteger(0);
        // Reject first two attempts, accept third
        var handle = generator.generate(h -> callCount.incrementAndGet() >= 3);

        assertThat(callCount.get()).isEqualTo(3);
        assertThat(handle).matches("[A-Z0-9]{6}");
    }
}
