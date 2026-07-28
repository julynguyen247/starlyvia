package org.example.notificationservice.websocket;

import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.Executor;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.realtime.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RedisNotificationRealtimePublisher implements NotificationRealtimePublisher {
    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic notificationRealtimeTopic;
    private final ObjectMapper objectMapper;
    private final Executor publishExecutor;

    public RedisNotificationRealtimePublisher(
            StringRedisTemplate redisTemplate,
            ChannelTopic notificationRealtimeTopic,
            ObjectMapper objectMapper,
            @Qualifier("notificationRealtimePublishExecutor") Executor publishExecutor
    ) {
        this.redisTemplate = redisTemplate;
        this.notificationRealtimeTopic = notificationRealtimeTopic;
        this.objectMapper = objectMapper;
        this.publishExecutor = publishExecutor;
    }

    @Override
    public void publish(NotificationRealtimeMessage message) {
        publishExecutor.execute(() -> publishToRedis(message));
    }

    private void publishToRedis(NotificationRealtimeMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(message);
            redisTemplate.convertAndSend(notificationRealtimeTopic.getTopic(), payload);
        } catch (Exception exception) {
            Object notificationId = message != null && message.notification() != null
                    ? message.notification().id()
                    : "unknown";
            log.warn(
                    "Unable to publish realtime notification {} to Redis",
                    notificationId,
                    exception
            );
        }
    }
}
