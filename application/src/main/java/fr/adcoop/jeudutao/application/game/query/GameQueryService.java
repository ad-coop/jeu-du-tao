package fr.adcoop.jeudutao.application.game.query;

import fr.adcoop.jeudutao.domain.game.exception.GameNotFoundException;

import java.util.List;

public class GameQueryService {
    private final GameQueryRepository gameQueryRepository;
    private final PlayerQueryRepository playerQueryRepository;

    public GameQueryService(GameQueryRepository gameQueryRepository, PlayerQueryRepository playerQueryRepository) {
        this.gameQueryRepository = gameQueryRepository;
        this.playerQueryRepository = playerQueryRepository;
    }

    public GameInfoView getGameInfo(String handle) {
        return gameQueryRepository.findGameInfo(handle)
                .orElseThrow(() -> new GameNotFoundException(handle));
    }

    public List<PlayerView> getPlayers(String handle) {
        if (!gameQueryRepository.existsByHandle(handle)) {
            throw new GameNotFoundException(handle);
        }
        return playerQueryRepository.findPlayersByGame(handle);
    }
}
