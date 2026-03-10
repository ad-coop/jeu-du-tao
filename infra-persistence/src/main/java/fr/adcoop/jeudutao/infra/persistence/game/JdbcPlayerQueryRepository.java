package fr.adcoop.jeudutao.infra.persistence.game;

import fr.adcoop.jeudutao.application.game.query.PlayerQueryRepository;
import fr.adcoop.jeudutao.application.game.query.PlayerView;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;

@Repository
public class JdbcPlayerQueryRepository implements PlayerQueryRepository {

    private final JdbcClient jdbcClient;

    public JdbcPlayerQueryRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public List<PlayerView> findPlayersByGame(String gameHandle) {
        return jdbcClient.sql("SELECT id, name, role FROM players WHERE game_handle = :gameHandle")
                .param("gameHandle", gameHandle)
                .query(JdbcPlayerQueryRepository::mapRow)
                .list();
    }

    private static PlayerView mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new PlayerView(
                rs.getString("id"),
                rs.getString("name"),
                rs.getString("role").toLowerCase(Locale.ROOT)
        );
    }
}
