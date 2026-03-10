package fr.adcoop.jeudutao.infra.persistence.game;

import fr.adcoop.jeudutao.application.game.query.GameInfoView;
import fr.adcoop.jeudutao.application.game.query.GameQueryRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcGameQueryRepository implements GameQueryRepository {

    private final JdbcClient jdbcClient;

    public JdbcGameQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<GameInfoView> findGameInfo(String handle) {
        return jdbcClient.sql("SELECT handle, state, password_hash, email FROM games WHERE handle = :handle")
                .param("handle", handle)
                .query(JdbcGameQueryRepository::mapRow)
                .optional();
    }

    @Override
    public boolean existsByHandle(String handle) {
        return jdbcClient.sql("SELECT COUNT(*) FROM games WHERE handle = :handle")
                .param("handle", handle)
                .query(Long.class)
                .single() > 0;
    }

    private static GameInfoView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new GameInfoView(
                rs.getString("handle"),
                rs.getString("state"),
                rs.getString("password_hash") != null,
                rs.getString("email") != null
        );
    }
}
