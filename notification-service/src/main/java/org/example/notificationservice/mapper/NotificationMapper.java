package org.example.notificationservice.mapper;

import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {
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
                notification.getSourceEventId(),
                notification.getSourceTopic(),
                notification.getStatus(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.getReadAt()
        );
    }
}
