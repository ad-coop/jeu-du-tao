package fr.adcoop.jeudutao.domain.game.port;

import fr.adcoop.jeudutao.domain.game.Player;

import java.util.Optional;

public interface PlayerCommandRepository {
    void save(Player player);
    Optional<Player> findById(String id);
    void deleteById(String id);
}
