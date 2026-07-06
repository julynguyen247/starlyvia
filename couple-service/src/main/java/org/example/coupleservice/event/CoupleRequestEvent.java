package org.example.coupleservice.event;

import java.util.UUID;

public record CoupleRequestEvent(
        UUID eventId,
        String eventType,
        int eventVersion,
        String occurredAt,
        UUID requestId,
        UUID requesterId,
        UUID receiverId,
        String status
) {
}
