package fr.adcoop.jeudutao.config;

import org.h2.tools.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.SQLException;

@Configuration
@ConditionalOnProperty(name = "h2.tcp-server.enabled", havingValue = "true")
public class H2TcpServerConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2TcpServer(@Value("${h2.tcp-server.port:9092}") int port) throws SQLException {
        return Server.createTcpServer(
                "-tcp",
                "-tcpPort", String.valueOf(port),
                "-tcpAllowOthers",
                "-ifNotExists"
        );
    }
}
