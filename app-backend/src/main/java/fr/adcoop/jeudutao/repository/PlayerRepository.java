package fr.adcoop.jeudutao.repository;

import fr.adcoop.jeudutao.domain.Player;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository {

    void save(Player player);

    Optional<Player> findById(String id);

    List<Player> findByGameHandle(String gameHandle);

    void deleteById(String id);
}
