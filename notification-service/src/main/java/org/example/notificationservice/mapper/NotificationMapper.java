package org.example.notificationservice.mapper;

import org.example.notificationservice.dto.CreateNotificationRequest;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.dto.UpdateNotificationRequest;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.entity.NotificationStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class NotificationMapper {
    public Notification toEntity(UUID recipientUserId, CreateNotificationRequest request) {
        return Notification.builder()
                .recipientUserId(recipientUserId)
                .actorUserId(request.getActorUserId())
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .resourceType(request.getResourceType())
                .resourceId(request.getResourceId())
                .status(NotificationStatus.UNREAD)
                .build();
    }

    public NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getRecipientUserId(),
                notification.getActorUserId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getResourceType(),
                notification.getResourceId(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getReadAt()
        );
    }

    public void updateEntity(Notification notification, UpdateNotificationRequest request) {
        if (request.getActorUserId() != null) {
            notification.setActorUserId(request.getActorUserId());
        }
        if (request.getType() != null) {
            notification.setType(request.getType());
        }
        if (request.getTitle() != null) {
            notification.setTitle(request.getTitle());
        }
        if (request.getMessage() != null) {
            notification.setMessage(request.getMessage());
        }
        if (request.getResourceType() != null) {
            notification.setResourceType(request.getResourceType());
        }
        if (request.getResourceId() != null) {
            notification.setResourceId(request.getResourceId());
        }
        if (request.getStatus() != null) {
            applyStatus(notification, request.getStatus());
        }
    }

    private void applyStatus(Notification notification, NotificationStatus status) {
        notification.setStatus(status);
        if (status == NotificationStatus.READ && notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        if (status == NotificationStatus.UNREAD) {
            notification.setReadAt(null);
        }
    }
}
