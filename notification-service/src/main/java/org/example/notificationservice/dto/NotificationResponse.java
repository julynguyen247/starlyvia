package org.example.notificationservice.dto;

import org.example.notificationservice.entity.NotificationStatus;
import org.example.notificationservice.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientUserId,
        UUID actorUserId,
        NotificationType type,
        String title,
        String message,
        String resourceType,
        UUID resourceId,
        UUID sourceEventId,
        String sourceTopic,
        NotificationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime readAt
) {
}
