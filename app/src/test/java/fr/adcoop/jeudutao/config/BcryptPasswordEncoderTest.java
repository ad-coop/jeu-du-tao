package fr.adcoop.jeudutao.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BcryptPasswordEncoderTest {

    private BcryptPasswordEncoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new BcryptPasswordEncoder();
    }

    @Test
    void encode_returnsNonNullHash() {
        var hash = encoder.encode("secret");

        assertThat(hash).isNotNull().isNotBlank();
    }

    @Test
    void encode_doesNotReturnPlainText() {
        var hash = encoder.encode("secret");

        assertThat(hash).isNotEqualTo("secret");
    }

    @Test
    void matches_withCorrectPassword_returnsTrue() {
        var hash = encoder.encode("secret");

        assertThat(encoder.matches("secret", hash)).isTrue();
    }

    @Test
    void matches_withWrongPassword_returnsFalse() {
        var hash = encoder.encode("secret");

        assertThat(encoder.matches("wrong", hash)).isFalse();
    }
}
