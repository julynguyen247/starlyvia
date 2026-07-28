package org.example.notificationservice.websocket;

import org.example.notificationservice.dto.NotificationRealtimeMessage;

public interface NotificationRealtimePublisher {
    void publish(NotificationRealtimeMessage message);
}
