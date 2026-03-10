package fr.adcoop.jeudutao.config;

import org.h2.tools.Server;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:tcp-test;DB_CLOSE_DELAY=-1",
        "h2.tcp-server.enabled=true",
        "h2.tcp-server.port=9093"
})
@ActiveProfiles("test")
class H2TcpServerConfigTest {

    @Autowired
    private Server h2TcpServer;

    @Test
    void h2TcpServer_whenEnabled_isRunning() {
        assertThat(h2TcpServer.isRunning(false)).isTrue();
    }

    @Test
    void h2TcpServer_whenEnabled_acceptsConnections() throws SQLException {
        try (var conn = DriverManager.getConnection(
                "jdbc:h2:tcp://localhost:9093/mem:tcp-test", "sa", "sa")) {
            assertThat(conn.isValid(1)).isTrue();
        }
    }
}
