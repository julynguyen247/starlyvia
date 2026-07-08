package org.example.notificationservice.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.notificationservice.entity.NotificationStatus;
import org.example.notificationservice.entity.NotificationType;

import java.util.UUID;

@Getter
@Setter
public class UpdateNotificationRequest {
    private UUID actorUserId;

    private NotificationType type;

    @Size(max = 160)
    private String title;

    @Size(max = 1000)
    private String message;

    @Size(max = 80)
    private String resourceType;

    private UUID resourceId;

    private NotificationStatus status;
}
