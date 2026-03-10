package fr.adcoop.jeudutao.application.port;

public interface MagicLinkSender {
    void sendLink(String email, String handle, String token);
}
