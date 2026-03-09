package fr.adcoop.jeudutao.service;

import fr.adcoop.jeudutao.domain.Game;
import fr.adcoop.jeudutao.domain.GameState;
import fr.adcoop.jeudutao.domain.Player;
import fr.adcoop.jeudutao.domain.PlayerRole;
import fr.adcoop.jeudutao.exception.GameAlreadyStartedException;
import fr.adcoop.jeudutao.exception.GameNotFoundException;
import fr.adcoop.jeudutao.exception.InvalidPasswordException;
import fr.adcoop.jeudutao.exception.PlayerNotFoundException;
import fr.adcoop.jeudutao.exception.UnauthorizedKickException;
import fr.adcoop.jeudutao.repository.GameRepository;
import fr.adcoop.jeudutao.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GameServiceTest {

    private GameRepository gameRepository;
    private PlayerRepository playerRepository;
    private HandleGenerator handleGenerator;
    private PasswordService passwordService;
    private MagicLinkService magicLinkService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        gameRepository = mock(GameRepository.class);
        playerRepository = mock(PlayerRepository.class);
        handleGenerator = mock(HandleGenerator.class);
        passwordService = mock(PasswordService.class);
        magicLinkService = mock(MagicLinkService.class);
        reset(gameRepository, playerRepository, handleGenerator, passwordService, magicLinkService);

        gameService = new GameService(gameRepository, playerRepository, handleGenerator, passwordService, magicLinkService);

        when(handleGenerator.generate(any())).thenReturn("GAME01");
    }

    @Test
    void createGame_withoutPasswordOrEmail_savesGameAndGuardian() {
        var result = gameService.createGame("Alice", null, null);

        assertThat(result.game().handle()).isEqualTo("GAME01");
        assertThat(result.game().passwordHash()).isNull();
        assertThat(result.game().email()).isNull();
        assertThat(result.game().state()).isEqualTo(GameState.WAITING);
        assertThat(result.guardian().name()).isEqualTo("Alice");
        assertThat(result.guardian().role()).isEqualTo(PlayerRole.GUARDIAN);

        verify(gameRepository).save(result.game());
        verify(playerRepository).save(result.guardian());
        verify(magicLinkService, never()).sendLink(anyString(), anyString(), anyString());
    }

    @Test
    void createGame_withPassword_hashesPassword() {
        when(passwordService.encode("secret")).thenReturn("hashed");

        var result = gameService.createGame("Alice", null, "secret");

        assertThat(result.game().passwordHash()).isEqualTo("hashed");
        verify(passwordService).encode("secret");
    }

    @Test
    void createGame_withEmail_generatesMagicLinkAndSends() {
        when(magicLinkService.generateToken()).thenReturn("abc123token");

        var result = gameService.createGame("Alice", "alice@example.com", null);

        assertThat(result.game().email()).isEqualTo("alice@example.com");
        assertThat(result.game().magicLinkToken()).isEqualTo("abc123token");
        assertThat(result.game().magicLinkExpiry()).isNotNull();
        verify(magicLinkService).sendLink("alice@example.com", "GAME01", "abc123token");
    }

    @Test
    void joinGame_withValidHandleAndNoPassword_createsPlayer() {
        var game = gameWithState("GAME01", GameState.WAITING, null);
        when(gameRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));

        var result = gameService.joinGame("GAME01", "Bob", null);

        assertThat(result.player().name()).isEqualTo("Bob");
        assertThat(result.player().role()).isEqualTo(PlayerRole.PLAYER);
        assertThat(result.player().gameHandle()).isEqualTo("GAME01");
        verify(playerRepository).save(result.player());
    }

    @Test
    void joinGame_whenGameNotFound_throwsGameNotFoundException() {
        when(gameRepository.findByHandle("NOPE00")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.joinGame("NOPE00", "Bob", null))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void joinGame_whenGameStarted_throwsGameAlreadyStartedException() {
        var game = gameWithState("GAME01", GameState.STARTED, null);
        when(gameRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> gameService.joinGame("GAME01", "Bob", null))
                .isInstanceOf(GameAlreadyStartedException.class);
    }

    @Test
    void joinGame_withWrongPassword_throwsInvalidPasswordException() {
        var game = gameWithState("GAME01", GameState.WAITING, "hashed");
        when(gameRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));
        when(passwordService.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> gameService.joinGame("GAME01", "Bob", "wrong"))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void joinGame_withNullPasswordWhenRequired_throwsInvalidPasswordException() {
        var game = gameWithState("GAME01", GameState.WAITING, "hashed");
        when(gameRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));

        assertThatThrownBy(() -> gameService.joinGame("GAME01", "Bob", null))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    void joinGame_withCorrectPassword_createsPlayer() {
        var game = gameWithState("GAME01", GameState.WAITING, "hashed");
        when(gameRepository.findByHandle("GAME01")).thenReturn(Optional.of(game));
        when(passwordService.matches("secret", "hashed")).thenReturn(true);

        var result = gameService.joinGame("GAME01", "Bob", "secret");

        assertThat(result.player().name()).isEqualTo("Bob");
    }

    @Test
    void kickPlayer_withGuardian_deletesTargetPlayer() {
        var guardian = new Player("g-id", "Alice", PlayerRole.GUARDIAN, "GAME01");
        when(playerRepository.findById("g-id")).thenReturn(Optional.of(guardian));

        gameService.kickPlayer("GAME01", "g-id", "p-id");

        verify(playerRepository).deleteById("p-id");
    }

    @Test
    void kickPlayer_whenRequesterNotFound_throwsPlayerNotFoundException() {
        when(playerRepository.findById("nobody")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gameService.kickPlayer("GAME01", "nobody", "p-id"))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    void kickPlayer_whenRequesterIsNotGuardian_throwsUnauthorizedKickException() {
        var player = new Player("p-id", "Bob", PlayerRole.PLAYER, "GAME01");
        when(playerRepository.findById("p-id")).thenReturn(Optional.of(player));

        assertThatThrownBy(() -> gameService.kickPlayer("GAME01", "p-id", "other-id"))
                .isInstanceOf(UnauthorizedKickException.class);
    }

    @Test
    void kickPlayer_whenRequesterIsGuardianOfDifferentGame_throwsUnauthorizedKickException() {
        var guardian = new Player("g-id", "Alice", PlayerRole.GUARDIAN, "OTHER1");
        when(playerRepository.findById("g-id")).thenReturn(Optional.of(guardian));

        assertThatThrownBy(() -> gameService.kickPlayer("GAME01", "g-id", "p-id"))
                .isInstanceOf(UnauthorizedKickException.class);
    }

    @Test
    void getPlayers_whenGameNotFound_throwsGameNotFoundException() {
        when(gameRepository.existsByHandle("NOPE00")).thenReturn(false);

        assertThatThrownBy(() -> gameService.getPlayers("NOPE00"))
                .isInstanceOf(GameNotFoundException.class);
    }

    @Test
    void getPlayers_returnsPlayersForGame() {
        when(gameRepository.existsByHandle("GAME01")).thenReturn(true);
        var players = List.of(
                new Player("p1", "Alice", PlayerRole.GUARDIAN, "GAME01"),
                new Player("p2", "Bob", PlayerRole.PLAYER, "GAME01")
        );
        when(playerRepository.findByGameHandle("GAME01")).thenReturn(players);

        var result = gameService.getPlayers("GAME01");

        assertThat(result).isEqualTo(players);
    }

    private Game gameWithState(String handle, GameState state, String passwordHash) {
        return new Game(handle, passwordHash, Instant.now(), state, "guardian-id", null, null, null);
    }
}
