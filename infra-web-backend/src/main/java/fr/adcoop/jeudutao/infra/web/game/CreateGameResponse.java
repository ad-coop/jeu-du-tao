package fr.adcoop.jeudutao.infra.web.game;

public record CreateGameResponse(String handle, String playerId, boolean passwordProtected, boolean hasEmail) {
}
