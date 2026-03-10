package fr.adcoop.jeudutao.infra.web.game;

public record RestoreGameResponse(String playerId, String playerName, boolean passwordProtected, boolean hasEmail) {
}
