package fr.adcoop.jeudutao.infra.web.websocket;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketConnectInterceptor implements ChannelInterceptor {

    private final WebSocketSessionManager sessionManager;

    public WebSocketConnectInterceptor(WebSocketSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            var sessionId = accessor.getSessionId();
            var playerId = accessor.getFirstNativeHeader("playerId");
            if (sessionId != null && playerId != null) {
                sessionManager.register(sessionId, playerId);
            }
        }
        return message;
    }
}
