package fr.adcoop.jeudutao.domain.game.port;

import fr.adcoop.jeudutao.domain.game.Game;

import java.util.Optional;

public interface GameRepository {
    void save(Game game);
    Optional<Game> findByHandle(String handle);
    boolean existsByHandle(String handle);
}
