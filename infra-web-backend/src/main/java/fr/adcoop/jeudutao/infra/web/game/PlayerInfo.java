package fr.adcoop.jeudutao.infra.web.game;

import fr.adcoop.jeudutao.domain.game.Player;

import java.util.Locale;

public record PlayerInfo(String id, String name, String role) {

    public static PlayerInfo from(Player player) {
        return new PlayerInfo(player.id(), player.name(), player.role().name().toLowerCase(Locale.ROOT));
    }
}
