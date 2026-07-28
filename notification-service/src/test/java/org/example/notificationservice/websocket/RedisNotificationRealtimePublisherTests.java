package org.example.notificationservice.websocket;

import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.NotificationStatus;
import org.example.notificationservice.entity.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisNotificationRealtimePublisherTests {
    private static final String CHANNEL = "starlyvia.notifications.created.v1";

    private final StringRedisTemplate redisTemplate = mock(StringRedisTemplate.class);
    private final ObjectMapper objectMapper = mock(ObjectMapper.class);
    private final RedisNotificationRealtimePublisher publisher = new RedisNotificationRealtimePublisher(
            redisTemplate,
            ChannelTopic.of(CHANNEL),
            objectMapper,
            Runnable::run
    );

    @Test
    void publishesSerializedMessageToConfiguredChannel() throws Exception {
        NotificationRealtimeMessage message = NotificationRealtimeMessage.created(notification());
        when(objectMapper.writeValueAsString(message)).thenReturn("{\"type\":\"NOTIFICATION_CREATED\"}");

        publisher.publish(message);

        verify(redisTemplate).convertAndSend(CHANNEL, "{\"type\":\"NOTIFICATION_CREATED\"}");
    }

    @Test
    void treatsRedisPublicationFailureAsBestEffort() throws Exception {
        NotificationRealtimeMessage message = NotificationRealtimeMessage.created(notification());
        when(objectMapper.writeValueAsString(message)).thenReturn("{\"type\":\"NOTIFICATION_CREATED\"}");
        when(redisTemplate.convertAndSend(CHANNEL, "{\"type\":\"NOTIFICATION_CREATED\"}"))
                .thenThrow(new IllegalStateException("Redis unavailable"));

        assertThatCode(() -> publisher.publish(message)).doesNotThrowAnyException();

        verify(redisTemplate).convertAndSend(CHANNEL, "{\"type\":\"NOTIFICATION_CREATED\"}");
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
