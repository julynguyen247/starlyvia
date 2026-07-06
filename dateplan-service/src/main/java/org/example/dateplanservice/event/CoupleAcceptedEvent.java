package org.example.dateplanservice.event;

import java.util.UUID;

public record CoupleAcceptedEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        String occurredAt,
        UUID requestId,
        UUID coupleId,
        UUID requesterId,
        UUID receiverId,
        UUID userId,
        UUID partnerId
) {
}
