package org.example.planservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.example.planservice.event.GroupEvent;
import org.example.planservice.service.GroupPlanCleanupService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class GroupEventConsumer {
    private final ObjectMapper objectMapper;
    private final GroupPlanCleanupService cleanupService;

    public GroupEventConsumer(ObjectMapper objectMapper, GroupPlanCleanupService cleanupService) {
        this.objectMapper = objectMapper;
        this.cleanupService = cleanupService;
    }

    @KafkaListener(topics = "${app.kafka.topics.group-events}")
    public void handleGroupEvent(String payload) {
        GroupEvent event = objectMapper.readValue(payload, GroupEvent.class);
        if (!"group.deleted".equals(event.eventType())) {
            return;
        }

        long deletedPlans = cleanupService.deleteByGroupId(event.groupId());
        log.info("Deleted {} plans for removed group {}", deletedPlans, event.groupId());
    }
}
