package fr.adcoop.jeudutao.infra.web.websocket;

import fr.adcoop.jeudutao.application.game.command.GameCommandService;
import fr.adcoop.jeudutao.application.game.query.GameQueryService;
import fr.adcoop.jeudutao.infra.web.game.PlayerInfo;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketDisconnectListener {

    private final WebSocketSessionManager sessionManager;
    private final GameCommandService commandService;
    private final GameQueryService queryService;
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketDisconnectListener(
            WebSocketSessionManager sessionManager,
            GameCommandService commandService,
            GameQueryService queryService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.sessionManager = sessionManager;
        this.commandService = commandService;
        this.queryService = queryService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        var sessionId = event.getSessionId();
        var maybePlayerId = sessionManager.getPlayerId(sessionId);

        if (maybePlayerId.isPresent()) {
            var playerId = maybePlayerId.get();
            var maybeResult = commandService.removePlayer(playerId);

            if (maybeResult.isPresent()) {
                var gameHandle = maybeResult.get().gameHandle();
                var remainingPlayers = queryService.getPlayers(gameHandle).stream()
                        .map(PlayerInfo::from)
                        .toList();
                messagingTemplate.convertAndSend("/topic/games/" + gameHandle + "/players", remainingPlayers);
            }

            sessionManager.remove(sessionId);
        }
    }
}
