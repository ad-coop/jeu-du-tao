package fr.adcoop.jeudutao.domain.game.exception;

public class PlayerNotFoundException extends RuntimeException {
    public PlayerNotFoundException(String playerId) {
        super("Player not found: " + playerId);
    }
}
