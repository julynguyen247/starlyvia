package org.example.notificationservice;

import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.event.PlanEvent;
import org.example.notificationservice.kafka.PlanEventConsumer;
import org.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class PlanEventConsumerTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NotificationService notificationService = mock(NotificationService.class);
    private final PlanEventConsumer consumer = new PlanEventConsumer(objectMapper, notificationService);

    @Test
    void createsNotificationsForOtherMembersOnly() {
        UUID eventId = UUID.randomUUID();
        UUID planId = UUID.randomUUID();
        UUID groupId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        UUID firstRecipientId = UUID.randomUUID();
        UUID secondRecipientId = UUID.randomUUID();
        PlanEvent event = new PlanEvent(
                eventId,
                "plan.updated",
                1,
                LocalDateTime.now().toString(),
                planId,
                groupId,
                actorId,
                "Weekend trip",
                List.of(actorId, firstRecipientId, secondRecipientId, firstRecipientId)
        );

        consumer.handlePlanEvent(objectMapper.writeValueAsString(event), "plan.events");

        verify(notificationService).createFromEvent(
                eq(eventId),
                eq("plan.events"),
                eq(firstRecipientId),
                eq(actorId),
                eq(NotificationType.PLAN_UPDATED),
                eq("Plan updated"),
                eq("'Weekend trip' was updated"),
                eq("PLAN"),
                eq(planId)
        );
        verify(notificationService).createFromEvent(
                eq(eventId),
                eq("plan.events"),
                eq(secondRecipientId),
                eq(actorId),
                eq(NotificationType.PLAN_UPDATED),
                eq("Plan updated"),
                eq("'Weekend trip' was updated"),
                eq("PLAN"),
                eq(planId)
        );
        verify(notificationService, never()).createFromEvent(
                eq(eventId),
                eq("plan.events"),
                eq(actorId),
                eq(actorId),
                eq(NotificationType.PLAN_UPDATED),
                eq("Plan updated"),
                eq("'Weekend trip' was updated"),
                eq("PLAN"),
                eq(planId)
        );
    }
}
