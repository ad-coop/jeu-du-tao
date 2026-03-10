package fr.adcoop.jeudutao.config;

import fr.adcoop.jeudutao.api.game.PlayerInfo;
import fr.adcoop.jeudutao.domain.game.port.PlayerRepository;
import fr.adcoop.jeudutao.application.game.GameService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketDisconnectListener {

    private final WebSocketSessionManager sessionManager;
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;
    private final PlayerRepository playerRepository;

    public WebSocketDisconnectListener(
            WebSocketSessionManager sessionManager,
            GameService gameService,
            SimpMessagingTemplate messagingTemplate,
            PlayerRepository playerRepository
    ) {
        this.sessionManager = sessionManager;
        this.gameService = gameService;
        this.messagingTemplate = messagingTemplate;
        this.playerRepository = playerRepository;
    }

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        var sessionId = event.getSessionId();
        var maybePlayerId = sessionManager.getPlayerId(sessionId);

        if (maybePlayerId.isPresent()) {
            var playerId = maybePlayerId.get();
            var maybePlayer = playerRepository.findById(playerId);

            if (maybePlayer.isPresent()) {
                var player = maybePlayer.get();
                var gameHandle = player.gameHandle();
                gameService.removePlayer(playerId);

                var remainingPlayers = gameService.getPlayers(gameHandle).stream()
                        .map(PlayerInfo::from)
                        .toList();
                messagingTemplate.convertAndSend("/topic/games/" + gameHandle + "/players", remainingPlayers);
            } else {
                gameService.removePlayer(playerId);
            }

            sessionManager.remove(sessionId);
        }
    }
}
