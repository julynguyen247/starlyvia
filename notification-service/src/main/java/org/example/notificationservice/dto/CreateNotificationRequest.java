package org.example.notificationservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.notificationservice.entity.NotificationType;

import java.util.UUID;

@Getter
@Setter
public class CreateNotificationRequest {
    private UUID actorUserId;

    @NotNull
    private NotificationType type;

    @NotBlank
    @Size(max = 160)
    private String title;

    @NotBlank
    @Size(max = 1000)
    private String message;

    @Size(max = 80)
    private String resourceType;

    private UUID resourceId;
}
