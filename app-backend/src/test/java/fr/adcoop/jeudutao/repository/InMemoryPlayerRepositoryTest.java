package fr.adcoop.jeudutao.repository;

import fr.adcoop.jeudutao.domain.Player;
import fr.adcoop.jeudutao.domain.PlayerRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryPlayerRepositoryTest {

    private InMemoryPlayerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryPlayerRepository();
    }

    @Test
    void findById_whenEmpty_returnsEmpty() {
        assertThat(repository.findById("player-1")).isEmpty();
    }

    @Test
    void save_thenFindById_returnsPlayer() {
        var player = new Player("player-1", "Alice", PlayerRole.GUARDIAN, "GAME01");

        repository.save(player);

        assertThat(repository.findById("player-1")).isPresent().contains(player);
    }

    @Test
    void findByGameHandle_whenNoMatch_returnsEmpty() {
        repository.save(new Player("p1", "Alice", PlayerRole.GUARDIAN, "GAME01"));

        assertThat(repository.findByGameHandle("OTHER1")).isEmpty();
    }

    @Test
    void findByGameHandle_returnsPlayersForHandle() {
        var player1 = new Player("p1", "Alice", PlayerRole.GUARDIAN, "GAME01");
        var player2 = new Player("p2", "Bob", PlayerRole.PLAYER, "GAME01");
        var otherPlayer = new Player("p3", "Charlie", PlayerRole.PLAYER, "OTHER1");

        repository.save(player1);
        repository.save(player2);
        repository.save(otherPlayer);

        var result = repository.findByGameHandle("GAME01");

        assertThat(result).containsExactlyInAnyOrder(player1, player2);
    }

    @Test
    void deleteById_removesPlayer() {
        repository.save(new Player("p1", "Alice", PlayerRole.GUARDIAN, "GAME01"));

        repository.deleteById("p1");

        assertThat(repository.findById("p1")).isEmpty();
    }

    @Test
    void deleteById_whenNotExists_doesNotThrow() {
        repository.deleteById("non-existent");
    }
}
