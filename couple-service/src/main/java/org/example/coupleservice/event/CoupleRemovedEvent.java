package org.example.coupleservice.event;

import java.util.UUID;

public record CoupleRemovedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        String occurredAt,
        UUID coupleId,
        UUID userId,
        UUID partnerId
) {
}
