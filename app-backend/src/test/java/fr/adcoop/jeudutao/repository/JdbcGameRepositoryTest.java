package fr.adcoop.jeudutao.repository;

import fr.adcoop.jeudutao.domain.Game;
import fr.adcoop.jeudutao.domain.GameState;
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

class JdbcGameRepositoryTest {

    private EmbeddedDatabase db;
    private JdbcGameRepository repository;

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
        repository = new JdbcGameRepository(JdbcClient.create(db));
    }

    @AfterEach
    void tearDown() {
        db.shutdown();
    }

    @Test
    void findByHandle_whenEmpty_returnsEmpty() {
        assertThat(repository.findByHandle("ABC123")).isEmpty();
    }

    @Test
    void save_thenFindByHandle_returnsGame() {
        var game = aGame("ABC123");

        repository.save(game);

        assertThat(repository.findByHandle("ABC123")).isPresent().contains(game);
    }

    @Test
    void save_overwritesExisting() {
        var game1 = aGame("ABC123");
        var game2 = new Game("ABC123", null, Instant.now().truncatedTo(ChronoUnit.MILLIS), GameState.STARTED, "other-id", "Bob", null, null, null);

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
        return new Game(handle, null, Instant.now().truncatedTo(ChronoUnit.MILLIS), GameState.WAITING, "guardian-id", "Alice", null, null, null);
    }
}
