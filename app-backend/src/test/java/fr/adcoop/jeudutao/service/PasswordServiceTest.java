package fr.adcoop.jeudutao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void encode_returnsNonNullHash() {
        var hash = passwordService.encode("secret");

        assertThat(hash).isNotNull().isNotBlank();
    }

    @Test
    void encode_doesNotReturnPlainText() {
        var hash = passwordService.encode("secret");

        assertThat(hash).isNotEqualTo("secret");
    }

    @Test
    void matches_withCorrectPassword_returnsTrue() {
        var hash = passwordService.encode("secret");

        assertThat(passwordService.matches("secret", hash)).isTrue();
    }

    @Test
    void matches_withWrongPassword_returnsFalse() {
        var hash = passwordService.encode("secret");

        assertThat(passwordService.matches("wrong", hash)).isFalse();
    }
}
