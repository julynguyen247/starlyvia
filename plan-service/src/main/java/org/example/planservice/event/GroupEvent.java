package org.example.planservice.event;

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
