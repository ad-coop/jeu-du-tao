package fr.adcoop.jeudutao.application.game.query;

import java.util.Optional;

public interface GameQueryRepository {
    Optional<GameInfoView> findGameInfo(String handle);
    boolean existsByHandle(String handle);
}
