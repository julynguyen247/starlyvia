package org.example.notificationservice.websocket;

import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.NotificationStatus;
import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.event.NotificationCreatedEvent;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class NotificationRealtimeEventListenerTests {
    @Test
    void publishesCreatedNotificationAfterReceivingDomainEvent() {
        NotificationRealtimePublisher publisher = mock(NotificationRealtimePublisher.class);
        NotificationRealtimeEventListener listener = new NotificationRealtimeEventListener(publisher);
        NotificationResponse notification = notification();

        listener.onNotificationCreated(new NotificationCreatedEvent(notification));

        ArgumentCaptor<NotificationRealtimeMessage> messageCaptor =
                ArgumentCaptor.forClass(NotificationRealtimeMessage.class);
        verify(publisher).publish(messageCaptor.capture());
        assertThat(messageCaptor.getValue().type()).isEqualTo("NOTIFICATION_CREATED");
        assertThat(messageCaptor.getValue().notification()).isEqualTo(notification);
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
