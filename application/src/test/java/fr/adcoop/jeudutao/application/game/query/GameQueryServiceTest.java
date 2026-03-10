package fr.adcoop.jeudutao.application.game.query;

import fr.adcoop.jeudutao.domain.game.exception.GameNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class GameQueryServiceTest {

    private GameQueryRepository gameQueryRepository;
    private PlayerQueryRepository playerQueryRepository;
    private GameQueryService gameQueryService;

    @BeforeEach
    void setUp() {
        gameQueryRepository = mock(GameQueryRepository.class);
        playerQueryRepository = mock(PlayerQueryRepository.class);
        reset(gameQueryRepository, playerQueryRepository);

        gameQueryService = new GameQueryService(gameQueryRepository, playerQueryRepository);
    }

    @Test
    void getGameInfo_whenFound_returnsView() {
        var view = new GameInfoView("GAME01", "WAITING", false, false);
        when(gameQueryRepository.findGameInfo("GAME01")).thenReturn(Optional.of(view));

        var result = gameQueryService.getGameInfo("GAME01");

        assertThat(result).isEqualTo(view);
    }

    @Test
    void getGameInfo_whenNotFound_throwsGameNotFoundException() {
        when(gameQueryRepository.findGameInfo("NOPE00")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameQueryService.getGameInfo("NOPE00"))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void getPlayers_whenGameNotFound_throwsGameNotFoundException() {
        when(gameQueryRepository.existsByHandle("NOPE00")).thenReturn(false);

        assertThatThrownBy(() -> gameQueryService.getPlayers("NOPE00"))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void getPlayers_returnsPlayersForGame() {
        when(gameQueryRepository.existsByHandle("GAME01")).thenReturn(true);
        var players = List.of(
                new PlayerView("p1", "Alice", "guardian"),
                new PlayerView("p2", "Bob", "player")
        );
        when(playerQueryRepository.findPlayersByGame("GAME01")).thenReturn(players);

        var result = gameQueryService.getPlayers("GAME01");

        assertThat(result).isEqualTo(players);
    }

    @Test
    void getPlayers_whenGameExistsWithNoPlayers_returnsEmptyList() {
        when(gameQueryRepository.existsByHandle("GAME01")).thenReturn(true);
        when(playerQueryRepository.findPlayersByGame("GAME01")).thenReturn(List.of());

        var result = gameQueryService.getPlayers("GAME01");

        assertThat(result).isEmpty();
    }
}
