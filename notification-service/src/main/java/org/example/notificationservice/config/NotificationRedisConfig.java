package org.example.notificationservice.config;

import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.websocket.RedisNotificationRealtimeSubscriber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "app.realtime.redis.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationRedisConfig {
    private static final int REALTIME_QUEUE_CAPACITY = 1_000;

    @Bean
    ChannelTopic notificationRealtimeTopic(
            @Value("${app.realtime.redis.channel:starlyvia.notifications.created.v1}") String channel
    ) {
        return ChannelTopic.of(channel);
    }

    @Bean
    RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            RedisNotificationRealtimeSubscriber subscriber,
            ChannelTopic notificationRealtimeTopic,
            @Qualifier("notificationRedisMessageExecutor") Executor messageExecutor,
            @Qualifier("notificationRedisSubscriptionExecutor") Executor subscriptionExecutor
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setTaskExecutor(messageExecutor);
        container.setSubscriptionExecutor(subscriptionExecutor);
        container.addMessageListener(subscriber, notificationRealtimeTopic);
        return container;
    }

    @Bean
    ThreadPoolTaskExecutor notificationRealtimePublishExecutor() {
        return boundedExecutor("notification-redis-publish-", 2, 2, REALTIME_QUEUE_CAPACITY);
    }

    @Bean
    ThreadPoolTaskExecutor notificationRedisMessageExecutor() {
        return boundedExecutor("notification-redis-message-", 2, 8, REALTIME_QUEUE_CAPACITY);
    }

    @Bean
    ThreadPoolTaskExecutor notificationRedisSubscriptionExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("notification-redis-subscription-");
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(0);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        return executor;
    }

    private ThreadPoolTaskExecutor boundedExecutor(
            String threadNamePrefix,
            int corePoolSize,
            int maxPoolSize,
            int queueCapacity
    ) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setRejectedExecutionHandler((task, threadPool) -> log.warn(
                "Dropping realtime notification task because executor {} is saturated",
                threadNamePrefix
        ));
        return executor;
    }
}
