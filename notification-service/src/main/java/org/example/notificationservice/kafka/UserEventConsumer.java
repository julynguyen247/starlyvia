package org.example.notificationservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.event.UserRegisteredEvent;
import org.example.notificationservice.service.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class UserEventConsumer {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public UserEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.topics.auth-events}")
    public void handleAuthEvent(String payload) {
        UserRegisteredEvent event = objectMapper.readValue(payload, UserRegisteredEvent.class);
        if ("user.registered".equals(event.eventType())) {
            handleUserRegistered(event);
            return;
        }
        log.info("Ignored auth event type {}: {}", event.eventType(), payload);
    }

    private void handleUserRegistered(UserRegisteredEvent event) {
        notificationService.createFromEvent(
                event.userId(),
                event.userId(),
                NotificationType.USER_REGISTERED,
                "Welcome to Starlyvia",
                "Your account has been created",
                "USER",
                event.userId()
        );
    }
}
