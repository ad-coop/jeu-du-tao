package fr.adcoop.jeudutao.config;

import fr.adcoop.jeudutao.application.game.GameService;
import fr.adcoop.jeudutao.application.port.MagicLinkSender;
import fr.adcoop.jeudutao.application.port.PasswordEncoder;
import fr.adcoop.jeudutao.domain.game.HandleGenerator;
import fr.adcoop.jeudutao.domain.game.port.GameRepository;
import fr.adcoop.jeudutao.domain.game.port.PlayerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public GameService gameService(
            GameRepository gameRepository,
            PlayerRepository playerRepository,
            HandleGenerator handleGenerator,
            PasswordEncoder passwordEncoder,
            MagicLinkSender magicLinkSender
    ) {
        return new GameService(gameRepository, playerRepository, handleGenerator, passwordEncoder, magicLinkSender);
    }

    @Bean
    public HandleGenerator handleGenerator() {
        return new HandleGenerator();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BcryptPasswordEncoder();
    }

    @Bean
    public MagicLinkSender magicLinkSender() {
        return new LoggingMagicLinkSender();
    }
}
