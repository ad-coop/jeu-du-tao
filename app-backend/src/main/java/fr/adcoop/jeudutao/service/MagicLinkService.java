package fr.adcoop.jeudutao.service;

import fr.adcoop.jeudutao.application.port.MagicLinkSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MagicLinkService implements MagicLinkSender {

    private static final Logger log = LoggerFactory.getLogger(MagicLinkService.class);

    // TODO: replace with actual email sending implementation
    @Override
    public void sendLink(String email, String handle, String token) {
        log.info("Magic link for game {}: /game/{}?token={}", handle, handle, token);
    }
}
