package fr.adcoop.jeudutao.config;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {

    private final ConcurrentHashMap<String, String> sessionToPlayer = new ConcurrentHashMap<>();

    public void register(String sessionId, String playerId) {
        sessionToPlayer.put(sessionId, playerId);
    }

    public Optional<String> getPlayerId(String sessionId) {
        return Optional.ofNullable(sessionToPlayer.get(sessionId));
    }

    public void remove(String sessionId) {
        sessionToPlayer.remove(sessionId);
    }
}
