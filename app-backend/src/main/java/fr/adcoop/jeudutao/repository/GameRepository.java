package fr.adcoop.jeudutao.repository;

import fr.adcoop.jeudutao.domain.Game;

import java.util.Optional;

public interface GameRepository {

    void save(Game game);

    Optional<Game> findByHandle(String handle);

    boolean existsByHandle(String handle);
}
