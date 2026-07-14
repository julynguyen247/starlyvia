package org.example.planservice.event;

import java.util.List;
import java.util.UUID;

public record PlanEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        String occurredAt,
        UUID planId,
        UUID groupId,
        UUID actorUserId,
        String planName,
        List<UUID> recipientUserIds
) {
}
