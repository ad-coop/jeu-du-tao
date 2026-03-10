package fr.adcoop.jeudutao.infra.persistence.game;

import fr.adcoop.jeudutao.domain.game.Player;
import fr.adcoop.jeudutao.domain.game.PlayerRole;
import fr.adcoop.jeudutao.domain.game.port.PlayerCommandRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class JdbcPlayerCommandRepository implements PlayerCommandRepository {

    private final JdbcClient jdbcClient;

    public JdbcPlayerCommandRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(Player player) {
        jdbcClient.sql("""
                MERGE INTO players (id, name, role, game_handle)
                KEY (id)
                VALUES (:id, :name, :role, :gameHandle)
                """)
                .param("id", player.id())
                .param("name", player.name())
                .param("role", player.role().name())
                .param("gameHandle", player.gameHandle())
                .update();
    }

    @Override
    public Optional<Player> findById(String id) {
        return jdbcClient.sql("SELECT * FROM players WHERE id = :id")
                .param("id", id)
                .query(JdbcPlayerCommandRepository::mapRow)
                .optional();
    }

    @Override
    public void deleteById(String id) {
        jdbcClient.sql("DELETE FROM players WHERE id = :id")
                .param("id", id)
                .update();
    }

    private static Player mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Player(
                rs.getString("id"),
                rs.getString("name"),
                PlayerRole.valueOf(rs.getString("role")),
                rs.getString("game_handle")
        );
    }
}
