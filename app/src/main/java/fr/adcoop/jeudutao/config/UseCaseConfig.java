package fr.adcoop.jeudutao.config;

import fr.adcoop.jeudutao.application.game.command.GameCommandService;
import fr.adcoop.jeudutao.application.game.query.GameQueryRepository;
import fr.adcoop.jeudutao.application.game.query.GameQueryService;
import fr.adcoop.jeudutao.application.game.query.PlayerQueryRepository;
import fr.adcoop.jeudutao.application.port.MagicLinkSender;
import fr.adcoop.jeudutao.application.port.PasswordEncoder;
import fr.adcoop.jeudutao.domain.game.HandleGenerator;
import fr.adcoop.jeudutao.domain.game.port.GameCommandRepository;
import fr.adcoop.jeudutao.domain.game.port.PlayerCommandRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public GameCommandService gameCommandService(
            GameCommandRepository gameRepository,
            PlayerCommandRepository playerRepository,
            HandleGenerator handleGenerator,
            PasswordEncoder passwordEncoder,
            MagicLinkSender magicLinkSender
    ) {
        return new GameCommandService(gameRepository, playerRepository, handleGenerator, passwordEncoder, magicLinkSender);
    }

    @Bean
    public GameQueryService gameQueryService(
            GameQueryRepository gameQueryRepository,
            PlayerQueryRepository playerQueryRepository
    ) {
        return new GameQueryService(gameQueryRepository, playerQueryRepository);
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
