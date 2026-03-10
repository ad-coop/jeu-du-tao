package fr.adcoop.jeudutao.repository;

import fr.adcoop.jeudutao.domain.game.Game;
import fr.adcoop.jeudutao.domain.game.GameState;
import fr.adcoop.jeudutao.domain.game.port.GameRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

@Repository
public class JdbcGameRepository implements GameRepository {

    private final JdbcClient jdbcClient;

    public JdbcGameRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(Game game) {
        jdbcClient.sql("""
                MERGE INTO games (handle, password_hash, created_at, state, guardian_id, guardian_name, magic_link_token, magic_link_expiry, email)
                KEY (handle)
                VALUES (:handle, :passwordHash, :createdAt, :state, :guardianId, :guardianName, :magicLinkToken, :magicLinkExpiry, :email)
                """)
                .param("handle", game.handle())
                .param("passwordHash", game.passwordHash())
                .param("createdAt", Timestamp.from(game.createdAt()))
                .param("state", game.state().name())
                .param("guardianId", game.guardianId())
                .param("guardianName", game.guardianName())
                .param("magicLinkToken", game.magicLinkToken())
                .param("magicLinkExpiry", game.magicLinkExpiry() != null ? Timestamp.from(game.magicLinkExpiry()) : null)
                .param("email", game.email())
                .update();
    }

    @Override
    public Optional<Game> findByHandle(String handle) {
        return jdbcClient.sql("SELECT * FROM games WHERE handle = :handle")
                .param("handle", handle)
                .query(JdbcGameRepository::mapRow)
                .optional();
    }

    @Override
    public boolean existsByHandle(String handle) {
        return jdbcClient.sql("SELECT COUNT(*) FROM games WHERE handle = :handle")
                .param("handle", handle)
                .query(Long.class)
                .single() > 0;
    }

    private static Game mapRow(ResultSet rs, int rowNum) throws SQLException {
        var magicLinkExpiry = rs.getTimestamp("magic_link_expiry");
        return new Game(
                rs.getString("handle"),
                rs.getString("password_hash"),
                rs.getTimestamp("created_at").toInstant(),
                GameState.valueOf(rs.getString("state")),
                rs.getString("guardian_id"),
                rs.getString("guardian_name"),
                rs.getString("magic_link_token"),
                magicLinkExpiry != null ? magicLinkExpiry.toInstant() : null,
                rs.getString("email")
        );
    }
}
