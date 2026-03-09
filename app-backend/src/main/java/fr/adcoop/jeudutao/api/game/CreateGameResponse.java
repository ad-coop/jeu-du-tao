package fr.adcoop.jeudutao.api.game;

public record CreateGameResponse(String handle, String playerId, boolean passwordProtected, boolean hasEmail) {
}
