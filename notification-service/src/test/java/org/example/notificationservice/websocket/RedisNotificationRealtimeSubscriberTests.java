package org.example.notificationservice.websocket;

import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.NotificationStatus;
import org.example.notificationservice.entity.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.Message;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisNotificationRealtimeSubscriberTests {
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final NotificationWebSocketHandler webSocketHandler = mock(NotificationWebSocketHandler.class);
    private final RedisNotificationRealtimeSubscriber subscriber = new RedisNotificationRealtimeSubscriber(
            objectMapper,
            webSocketHandler
    );

    @Test
    void deliversRedisMessageToRecipientSessions() throws Exception {
        ObjectMapper actualObjectMapper = new ObjectMapper();
        Message redisMessage = mock(Message.class);
        NotificationRealtimeMessage message = NotificationRealtimeMessage.created(notification());
        byte[] payload = actualObjectMapper.writeValueAsBytes(message);
        when(redisMessage.getBody()).thenReturn(payload);

        RedisNotificationRealtimeSubscriber actualSubscriber = new RedisNotificationRealtimeSubscriber(
                actualObjectMapper,
                webSocketHandler
        );

        actualSubscriber.onMessage(redisMessage, null);

        verify(webSocketHandler).sendToUser(message.notification().recipientUserId(), message);
    }

    @Test
    void ignoresMalformedRedisMessage() throws Exception {
        byte[] payload = "invalid".getBytes(StandardCharsets.UTF_8);
        Message redisMessage = mock(Message.class);
        when(redisMessage.getBody()).thenReturn(payload);
        when(objectMapper.readValue(payload, NotificationRealtimeMessage.class))
                .thenThrow(new IllegalArgumentException("Malformed payload"));

        subscriber.onMessage(redisMessage, null);

        verify(webSocketHandler, never()).sendToUser(any(), any());
    }

    private NotificationResponse notification() {
        return new NotificationResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                NotificationType.PLAN_UPDATED,
                "Plan updated",
                "A plan was updated",
                "PLAN",
                UUID.randomUUID(),
                UUID.randomUUID(),
                "plan.events",
                NotificationStatus.UNREAD,
                null,
                null,
                null
        );
    }
}
