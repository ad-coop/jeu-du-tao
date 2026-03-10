package fr.adcoop.jeudutao.api.game;

public record RestoreGameResponse(String playerId, String playerName, boolean passwordProtected, boolean hasEmail) {
}
