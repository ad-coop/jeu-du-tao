package fr.adcoop.jeudutao.config;

import fr.adcoop.jeudutao.domain.game.HandleGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DomainConfig {

    @Bean
    public HandleGenerator handleGenerator() {
        return new HandleGenerator();
    }
}
