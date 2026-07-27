package org.example.notificationservice.event;

import org.example.notificationservice.dto.NotificationResponse;

public record NotificationCreatedEvent(NotificationResponse notification) {
}
