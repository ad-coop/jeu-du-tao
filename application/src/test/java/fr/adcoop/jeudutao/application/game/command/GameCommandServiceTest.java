package fr.adcoop.jeudutao.application.game.command;

import fr.adcoop.jeudutao.application.port.MagicLinkSender;
import fr.adcoop.jeudutao.application.port.PasswordEncoder;
import fr.adcoop.jeudutao.domain.game.Game;
import fr.adcoop.jeudutao.domain.game.GameState;
import fr.adcoop.jeudutao.domain.game.HandleGenerator;
import fr.adcoop.jeudutao.domain.game.Player;
import fr.adcoop.jeudutao.domain.game.PlayerRole;
import fr.adcoop.jeudutao.domain.game.exception.GameAlreadyStartedException;
import fr.adcoop.jeudutao.domain.game.exception.GameNotFoundException;
import fr.adcoop.jeudutao.domain.game.exception.InvalidMagicLinkException;
import fr.adcoop.jeudutao.domain.game.exception.InvalidPasswordException;
import fr.adcoop.jeudutao.domain.game.exception.PlayerNotFoundException;
import fr.adcoop.jeudutao.domain.game.exception.UnauthorizedKickException;
import fr.adcoop.jeudutao.domain.game.port.GameCommandRepository;
import fr.adcoop.jeudutao.domain.game.port.PlayerCommandRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameCommandServiceTest {

    private GameCommandRepository gameCommandRepository;
    private PlayerCommandRepository playerCommandRepository;
    private HandleGenerator handleGenerator;
    private PasswordEncoder passwordEncoder;
    private MagicLinkSender magicLinkSender;
    private GameCommandService gameCommandService;

    @BeforeEach
    void setUp() {
        gameCommandRepository = mock(GameCommandRepository.class);
        playerCommandRepository = mock(PlayerCommandRepository.class);
        handleGenerator = mock(HandleGenerator.class);
        passwordEncoder = mock(PasswordEncoder.class);
        magicLinkSender = mock(MagicLinkSender.class);
        reset(gameCommandRepository, playerCommandRepository, handleGenerator, passwordEncoder, magicLinkSender);

        gameCommandService = new GameCommandService(
                gameCommandRepository,
                playerCommandRepository,
                handleGenerator,
                passwordEncoder,
                magicLinkSender
        );

        when(handleGenerator.generate(any())).thenReturn("GAME01");
    }

    @Test
    void createGame_withoutPasswordOrEmail_returnsHandleAndGuardianId() {
        var result = gameCommandService.createGame("Alice", null, null);

        assertThat(result.handle()).isEqualTo("GAME01");
        assertThat(result.guardianId()).isNotNull().isNotBlank();
        assertThat(result.passwordProtected()).isFalse();
        assertThat(result.hasEmail()).isFalse();

        verify(gameCommandRepository).save(any());
        verify(playerCommandRepository).save(any());
        verify(magicLinkSender, never()).sendLink(anyString(), anyString(), anyString());
    }

    @Test
    void createGame_withPassword_returnsPasswordProtectedTrue() {
        when(passwordEncoder.encode("secret")).thenReturn("hashed");

        var result = gameCommandService.createGame("Alice", null, "secret");

        assertThat(result.passwordProtected()).isTrue();
        verify(passwordEncoder).encode("secret");
    }

    @Test
    void createGame_withEmail_returnsHasEmailTrue() {
        var result = gameCommandService.createGame("Alice", "alice@example.com", null);

        assertThat(result.hasEmail()).isTrue();
        verify(magicLinkSender).sendLink(eq("alice@example.com"), eq("GAME01"), anyString());
    }

    @Test
    void joinGame_withValidHandleAndNoPassword_returnsPlayerId() {
        var game = gameWithState("GAME01", GameState.WAITING, null);
        when(gameCommandRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));

        var result = gameCommandService.joinGame("GAME01", "Bob", null);

        assertThat(result.playerId()).isNotNull().isNotBlank();
        verify(playerCommandRepository).save(any());
    }

    @Test
    void joinGame_whenGameNotFound_throwsGameNotFoundException() {
        when(gameCommandRepository.findByHandle("NOPE00")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameCommandService.joinGame("NOPE00", "Bob", null))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void joinGame_whenGameStarted_throwsGameAlreadyStartedException() {
        var game = gameWithState("GAME01", GameState.STARTED, null);
        when(gameCommandRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> gameCommandService.joinGame("GAME01", "Bob", null))
                .isInstanceOf(GameAlreadyStartedException.class);
    }

    @Test
    void joinGame_withWrongPassword_throwsInvalidPasswordException() {
        var game = gameWithState("GAME01", GameState.WAITING, "hashed");
        when(gameCommandRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> gameCommandService.joinGame("GAME01", "Bob", "wrong"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void kickPlayer_withGuardian_deletesTargetPlayer() {
        var guardian = new Player("g-id", "Alice", PlayerRole.GUARDIAN, "GAME01");
        when(playerCommandRepository.findById("g-id")).thenReturn(Optional.of(guardian));

        gameCommandService.kickPlayer("GAME01", "g-id", "p-id");

        verify(playerCommandRepository).deleteById("p-id");
    }

    @Test
    void kickPlayer_whenRequesterNotFound_throwsPlayerNotFoundException() {
        when(playerCommandRepository.findById("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameCommandService.kickPlayer("GAME01", "nobody", "p-id"))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    void kickPlayer_whenRequesterIsNotGuardian_throwsUnauthorizedKickException() {
        var player = new Player("p-id", "Bob", PlayerRole.PLAYER, "GAME01");
        when(playerCommandRepository.findById("p-id")).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> gameCommandService.kickPlayer("GAME01", "p-id", "other-id"))
                .isInstanceOf(UnauthorizedKickException.class);
    }

    @Test
    void removePlayer_whenPlayerExists_returnsGameHandle() {
        var player = new Player("p-id", "Bob", PlayerRole.PLAYER, "GAME01");
        when(playerCommandRepository.findById("p-id")).thenReturn(Optional.of(player));

        var result = gameCommandService.removePlayer("p-id");

        assertThat(result).isPresent();
        assertThat(result.get().gameHandle()).isEqualTo("GAME01");
        verify(playerCommandRepository).deleteById("p-id");
    }

    @Test
    void removePlayer_whenPlayerNotFound_returnsEmpty() {
        when(playerCommandRepository.findById("unknown")).thenReturn(Optional.empty());

        var result = gameCommandService.removePlayer("unknown");

        assertThat(result).isEmpty();
        verify(playerCommandRepository).deleteById("unknown");
    }

    @Test
    void restoreGuardian_whenTokenValid_returnsGuardianInfo() {
        var token = "valid-token";
        var expiry = Instant.now().plus(1, ChronoUnit.HOURS);
        var game = gameWithMagicLink(token, expiry);
        var guardian = new Player("guardian-id", "Alice", PlayerRole.GUARDIAN, "GAME01");
        when(gameCommandRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));
        when(playerCommandRepository.findById("guardian-id")).thenReturn(Optional.of(guardian));

        var result = gameCommandService.restoreGuardian("GAME01", token);

        assertThat(result.playerId()).isEqualTo("guardian-id");
        assertThat(result.playerName()).isEqualTo("Alice");
    }

    @Test
    void restoreGuardian_whenTokenExpired_throwsInvalidMagicLinkException() {
        var token = "expired-token";
        var expiry = Instant.now().minus(1, ChronoUnit.HOURS);
        var game = gameWithMagicLink(token, expiry);
        when(gameCommandRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> gameCommandService.restoreGuardian("GAME01", token))
                .isInstanceOf(InvalidMagicLinkException.class);
    }

    private Game gameWithState(String handle, GameState state, String passwordHash) {
        return new Game(handle, passwordHash, Instant.now(), state, "guardian-id", "Alice", null, null, null);
    }

    private Game gameWithMagicLink(String token, Instant expiry) {
        return new Game("GAME01", null, Instant.now(), GameState.WAITING, "guardian-id", "Alice", token, expiry, null);
    }
}
