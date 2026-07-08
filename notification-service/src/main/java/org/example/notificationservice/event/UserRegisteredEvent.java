package org.example.notificationservice.event;

import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        String occurredAt,
        UUID userId,
        String email,
        String username,
        String role,
        String avatarUrl,
        String bio
) {
}
