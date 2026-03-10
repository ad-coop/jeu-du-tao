package fr.adcoop.jeudutao.application.game.query;

import java.util.List;

public interface PlayerQueryRepository {
    List<PlayerView> findPlayersByGame(String gameHandle);
}
