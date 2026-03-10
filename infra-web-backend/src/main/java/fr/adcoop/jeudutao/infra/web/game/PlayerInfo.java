package fr.adcoop.jeudutao.infra.web.game;

import fr.adcoop.jeudutao.application.game.query.PlayerView;

public record PlayerInfo(String id, String name, String role) {

    public static PlayerInfo from(PlayerView view) {
        return new PlayerInfo(view.id(), view.name(), view.role());
    }
}
