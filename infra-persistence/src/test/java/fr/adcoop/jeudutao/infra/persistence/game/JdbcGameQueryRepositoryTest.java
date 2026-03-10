package fr.adcoop.jeudutao.infra.persistence.game;

import fr.adcoop.jeudutao.application.game.query.GameInfoView;
import fr.adcoop.jeudutao.domain.game.Game;
import fr.adcoop.jeudutao.domain.game.GameState;
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

class JdbcGameQueryRepositoryTest {

    private EmbeddedDatabase db;
    private JdbcGameCommandRepository commandRepository;
    private JdbcGameQueryRepository queryRepository;

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
        commandRepository = new JdbcGameCommandRepository(jdbcClient);
        queryRepository = new JdbcGameQueryRepository(jdbcClient);
    }

    @AfterEach
    void tearDown() {
        db.shutdown();
    }

    @Test
    void findGameInfo_whenNotFound_returnsEmpty() {
        assertThat(queryRepository.findGameInfo("NOTEXIST")).isEmpty();
    }

    @Test
    void findGameInfo_whenFound_returnsView() {
        var createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        commandRepository.save(new Game("ABC123", null, createdAt, GameState.WAITING, "guardian-id", "Alice", null, null, null));

        var result = queryRepository.findGameInfo("ABC123");

        assertThat(result).isPresent();
        var view = result.get();
        assertThat(view.handle()).isEqualTo("ABC123");
        assertThat(view.state()).isEqualTo("WAITING");
        assertThat(view.passwordProtected()).isFalse();
        assertThat(view.hasEmail()).isFalse();
    }

    @Test
    void findGameInfo_withPassword_returnsPasswordProtectedTrue() {
        var createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        commandRepository.save(new Game("ABC123", "hashed-password", createdAt, GameState.WAITING, "guardian-id", "Alice", null, null, null));

        var result = queryRepository.findGameInfo("ABC123");

        assertThat(result).isPresent();
        assertThat(result.get().passwordProtected()).isTrue();
    }

    @Test
    void findGameInfo_withEmail_returnsHasEmailTrue() {
        var createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        commandRepository.save(new Game("ABC123", null, createdAt, GameState.WAITING, "guardian-id", "Alice", null, null, "alice@example.com"));

        var result = queryRepository.findGameInfo("ABC123");

        assertThat(result).isPresent();
        assertThat(result.get().hasEmail()).isTrue();
    }

    @Test
    void existsByHandle_whenNotExists_returnsFalse() {
        assertThat(queryRepository.existsByHandle("NOTEXIST")).isFalse();
    }

    @Test
    void existsByHandle_whenExists_returnsTrue() {
        var createdAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        commandRepository.save(new Game("ABC123", null, createdAt, GameState.WAITING, "guardian-id", "Alice", null, null, null));

        assertThat(queryRepository.existsByHandle("ABC123")).isTrue();
    }
}
