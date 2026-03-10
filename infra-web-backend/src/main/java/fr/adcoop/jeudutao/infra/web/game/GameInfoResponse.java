package fr.adcoop.jeudutao.infra.web.game;

public record GameInfoResponse(String handle, String state, boolean passwordProtected) {
}
