package org.example.notificationservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.event.PlanEvent;
import org.example.notificationservice.service.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class PlanEventConsumer {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public PlanEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.topics.plan-events}")
    public void handlePlanEvent(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        PlanEvent event = objectMapper.readValue(payload, PlanEvent.class);
        NotificationType type = notificationType(event.eventType());
        if (type == null) {
            log.info("Ignored plan event type {}: {}", event.eventType(), payload);
            return;
        }

        List<UUID> recipients = event.recipientUserIds() == null
                ? List.of()
                : event.recipientUserIds();
        recipients.stream()
                .filter(recipientId -> !recipientId.equals(event.actorUserId()))
                .distinct()
                .forEach(recipientId -> notificationService.createFromEvent(
                        event.eventId(),
                        topic,
                        recipientId,
                        event.actorUserId(),
                        type,
                        title(type),
                        message(type, event.planName()),
                        "PLAN",
                        event.planId()
                ));
    }

    private NotificationType notificationType(String eventType) {
        return switch (eventType) {
            case "plan.created" -> NotificationType.PLAN_CREATED;
            case "plan.updated" -> NotificationType.PLAN_UPDATED;
            case "plan.deleted" -> NotificationType.PLAN_DELETED;
            default -> null;
        };
    }

    private String title(NotificationType type) {
        return switch (type) {
            case PLAN_CREATED -> "Plan created";
            case PLAN_UPDATED -> "Plan updated";
            case PLAN_DELETED -> "Plan deleted";
            default -> throw new IllegalArgumentException("Unsupported plan notification type: " + type);
        };
    }

    private String message(NotificationType type, String planName) {
        String safePlanName = planName == null || planName.isBlank() ? "A group plan" : "'" + planName + "'";
        return switch (type) {
            case PLAN_CREATED -> safePlanName + " was created";
            case PLAN_UPDATED -> safePlanName + " was updated";
            case PLAN_DELETED -> safePlanName + " was deleted";
            default -> throw new IllegalArgumentException("Unsupported plan notification type: " + type);
        };
    }
}
