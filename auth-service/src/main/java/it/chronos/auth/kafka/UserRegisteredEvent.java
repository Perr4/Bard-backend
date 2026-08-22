package it.chronos.auth.kafka;

import java.time.Instant;
import java.util.UUID;

public record UserRegisteredEvent(
        UUID userId,
        String email,
        String fullName,
        Instant registeredAt) {
}
