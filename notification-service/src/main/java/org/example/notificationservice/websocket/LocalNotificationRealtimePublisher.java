package org.example.notificationservice.websocket;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.realtime.redis.enabled", havingValue = "false")
public class LocalNotificationRealtimePublisher implements NotificationRealtimePublisher {
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public void publish(NotificationRealtimeMessage message) {
        notificationWebSocketHandler.sendToUser(message.notification().recipientUserId(), message);
    }
}
