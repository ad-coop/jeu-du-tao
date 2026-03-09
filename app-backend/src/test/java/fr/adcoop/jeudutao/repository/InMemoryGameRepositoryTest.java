package fr.adcoop.jeudutao.repository;

import fr.adcoop.jeudutao.domain.Game;
import fr.adcoop.jeudutao.domain.GameState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryGameRepositoryTest {

    private InMemoryGameRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryGameRepository();
    }

    @Test
    void findByHandle_whenEmpty_returnsEmpty() {
        var result = repository.findByHandle("ABC123");

        assertThat(result).isEmpty();
    }

    @Test
    void save_thenFindByHandle_returnsGame() {
        var game = aGame("ABC123");

        repository.save(game);
        var result = repository.findByHandle("ABC123");

        assertThat(result).isPresent().contains(game);
    }

    @Test
    void save_overwritesExisting() {
        var game1 = aGame("ABC123");
        var game2 = new Game("ABC123", null, Instant.now(), GameState.STARTED, "other-id", null, null, null);

        repository.save(game1);
        repository.save(game2);

        assertThat(repository.findByHandle("ABC123")).isPresent().contains(game2);
    }

    @Test
    void existsByHandle_whenNotExists_returnsFalse() {
        assertThat(repository.existsByHandle("ABC123")).isFalse();
    }

    @Test
    void existsByHandle_whenExists_returnsTrue() {
        repository.save(aGame("ABC123"));

        assertThat(repository.existsByHandle("ABC123")).isTrue();
    }

    private Game aGame(String handle) {
        return new Game(handle, null, Instant.now(), GameState.WAITING, "guardian-id", null, null, null);
    }
}
