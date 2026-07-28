package org.example.notificationservice.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.realtime.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisNotificationRealtimeSubscriber implements MessageListener {
    private final ObjectMapper objectMapper;
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    @Override
    public void onMessage(Message redisMessage, byte[] pattern) {
        try {
            NotificationRealtimeMessage message = objectMapper.readValue(
                    redisMessage.getBody(),
                    NotificationRealtimeMessage.class
            );
            if (message.notification() == null || message.notification().recipientUserId() == null) {
                log.warn("Ignoring realtime notification from Redis without a recipient");
                return;
            }
            notificationWebSocketHandler.sendToUser(message.notification().recipientUserId(), message);
        } catch (Exception exception) {
            log.warn("Unable to process realtime notification from Redis", exception);
        }
    }
}
