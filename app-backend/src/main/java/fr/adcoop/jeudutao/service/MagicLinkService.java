package fr.adcoop.jeudutao.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.HexFormat;

@Component
public class MagicLinkService {

    private static final Logger log = LoggerFactory.getLogger(MagicLinkService.class);

    private final SecureRandom random = new SecureRandom();

    public String generateToken() {
        var bytes = new byte[16];
        random.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }

    // TODO: replace with actual email sending implementation
    public void sendLink(String email, String handle, String token) {
        log.info("Magic link for game {}: /game/{}?token={}", handle, handle, token);
    }
}
