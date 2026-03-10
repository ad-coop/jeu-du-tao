package fr.adcoop.jeudutao.config;

import fr.adcoop.jeudutao.application.port.MagicLinkSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingMagicLinkSender implements MagicLinkSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingMagicLinkSender.class);

    // TODO: replace with actual email sending implementation
    @Override
    public void sendLink(String email, String handle, String token) {
        log.info("Magic link for game {}: /game/{}?token={}", handle, handle, token);
    }
}
