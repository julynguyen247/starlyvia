package org.example.notificationservice.event;

import java.util.UUID;

public record GroupEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        String occurredAt,
        UUID groupId,
        UUID actorId,
        UUID targetUserId
) {
}
