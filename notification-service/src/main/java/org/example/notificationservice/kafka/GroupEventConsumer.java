package org.example.notificationservice.kafka;

import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.event.GroupEvent;
import org.example.notificationservice.service.NotificationService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class GroupEventConsumer {
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    public GroupEventConsumer(ObjectMapper objectMapper, NotificationService notificationService) {
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.topics.group-events}")
    public void handleGroupEvent(
            String payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic
    ) {
        GroupEvent event = objectMapper.readValue(payload, GroupEvent.class);
        switch (event.eventType()) {
            case "group.invitation.created" -> handleGroupInvitationCreated(event, topic);
            case "group.member.added" -> handleGroupMemberAdded(event, topic);
            case "group.member.removed" -> handleGroupMemberRemoved(event, topic);
            default -> log.info("Ignored group event type {}: {}", event.eventType(), payload);
        }
    }

    private void handleGroupInvitationCreated(GroupEvent event, String topic) {
        notificationService.createFromEvent(
                event.eventId(),
                topic,
                event.targetUserId(),
                event.actorId(),
                NotificationType.GROUP_INVITATION_CREATED,
                "Group invitation",
                "You have been invited to a group",
                "GROUP",
                event.groupId()
        );
    }

    private void handleGroupMemberAdded(GroupEvent event, String topic) {
        notificationService.createFromEvent(
                event.eventId(),
                topic,
                event.targetUserId(),
                event.actorId(),
                NotificationType.GROUP_MEMBER_ADDED,
                "Group member added",
                "You have been added to a group",
                "GROUP",
                event.groupId()
        );
    }

    private void handleGroupMemberRemoved(GroupEvent event, String topic) {
        notificationService.createFromEvent(
                event.eventId(),
                topic,
                event.targetUserId(),
                event.actorId(),
                NotificationType.GROUP_MEMBER_REMOVED,
                "Group member removed",
                "You have been removed from a group",
                "GROUP",
                event.groupId()
        );
    }
}
