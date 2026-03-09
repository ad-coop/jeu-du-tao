package fr.adcoop.jeudutao.domain;

import java.time.Instant;

public record Game(
        String handle,
        String passwordHash,
        Instant createdAt,
        GameState state,
        String guardianId,
        String magicLinkToken,
        Instant magicLinkExpiry,
        String email
) {
}
