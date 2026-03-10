package fr.adcoop.jeudutao.repository;

import fr.adcoop.jeudutao.domain.game.Game;
import fr.adcoop.jeudutao.domain.game.GameState;
import fr.adcoop.jeudutao.domain.game.Player;
import fr.adcoop.jeudutao.domain.game.PlayerRole;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class JdbcPlayerRepositoryTest {

    private EmbeddedDatabase db;
    private JdbcPlayerRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        db = new EmbeddedDatabaseBuilder()
                .generateUniqueName(true)
                .setType(EmbeddedDatabaseType.H2)
                .build();
        try (var liquibase = new Liquibase(
                "db/changelog/db.changelog-master.yaml",
                new ClassLoaderResourceAccessor(),
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(db.getConnection()))
        )) {
            liquibase.update(new Contexts(), new LabelExpression());
        }
        var jdbcClient = JdbcClient.create(db);
        var gameRepository = new JdbcGameRepository(jdbcClient);
        repository = new JdbcPlayerRepository(jdbcClient);

        var createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        gameRepository.save(new Game("GAME01", null, createdAt, GameState.WAITING, "guardian-id", "Alice", null, null, null));
        gameRepository.save(new Game("OTHER1", null, createdAt, GameState.WAITING, "guardian-id", "Alice", null, null, null));
    }

    @AfterEach
    void tearDown() {
        db.shutdown();
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

        assertThat(repository.findByGameHandle("GAME01")).containsExactlyInAnyOrder(player1, player2);
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
