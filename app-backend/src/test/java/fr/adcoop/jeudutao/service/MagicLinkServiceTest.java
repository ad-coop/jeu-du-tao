package fr.adcoop.jeudutao.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MagicLinkServiceTest {

    private MagicLinkService magicLinkService;

    @BeforeEach
    void setUp() {
        magicLinkService = new MagicLinkService();
    }

    @Test
    void generateToken_returns32CharHexString() {
        var token = magicLinkService.generateToken();

        assertThat(token).hasSize(32).matches("[0-9a-f]{32}");
    }

    @Test
    void generateToken_producesUniqueTokens() {
        var token1 = magicLinkService.generateToken();
        var token2 = magicLinkService.generateToken();

        assertThat(token1).isNotEqualTo(token2);
    }
}
