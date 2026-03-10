package fr.adcoop.jeudutao.infra.persistence.game;

import fr.adcoop.jeudutao.application.game.query.PlayerView;
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

class JdbcPlayerQueryRepositoryTest {

    private EmbeddedDatabase db;
    private JdbcGameCommandRepository gameCommandRepository;
    private JdbcPlayerCommandRepository playerCommandRepository;
    private JdbcPlayerQueryRepository queryRepository;

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
        gameCommandRepository = new JdbcGameCommandRepository(jdbcClient);
        playerCommandRepository = new JdbcPlayerCommandRepository(jdbcClient);
        queryRepository = new JdbcPlayerQueryRepository(jdbcClient);

        var createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        gameCommandRepository.save(new Game("GAME01", null, createdAt, GameState.WAITING, "guardian-id", "Alice", null, null, null));
        gameCommandRepository.save(new Game("OTHER1", null, createdAt, GameState.WAITING, "guardian-id", "Alice", null, null, null));
    }

    @AfterEach
    void tearDown() {
        db.shutdown();
    }

    @Test
    void findPlayersByGame_whenNone_returnsEmpty() {
        assertThat(queryRepository.findPlayersByGame("GAME01")).isEmpty();
    }

    @Test
    void findPlayersByGame_returnsViewsForGame() {
        playerCommandRepository.save(new Player("p1", "Alice", PlayerRole.GUARDIAN, "GAME01"));
        playerCommandRepository.save(new Player("p2", "Bob", PlayerRole.PLAYER, "GAME01"));
        playerCommandRepository.save(new Player("p3", "Charlie", PlayerRole.PLAYER, "OTHER1"));

        var result = queryRepository.findPlayersByGame("GAME01");

        assertThat(result).containsExactlyInAnyOrder(
                new PlayerView("p1", "Alice", "guardian"),
                new PlayerView("p2", "Bob", "player")
        );
    }

    @Test
    void findPlayersByGame_roleLowercased() {
        playerCommandRepository.save(new Player("p1", "Alice", PlayerRole.GUARDIAN, "GAME01"));

        var result = queryRepository.findPlayersByGame("GAME01");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).role()).isEqualTo("guardian");
    }
}
