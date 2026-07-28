package org.example.notificationservice.websocket;

import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.NotificationStatus;
import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.event.NotificationCreatedEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@ActiveProfiles("test")
class NotificationRealtimeTransactionTests {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private NotificationRealtimePublisher notificationRealtimePublisher;

    @Test
    void publishesRealtimeMessageOnlyAfterTransactionCommit() {
        NotificationResponse notification = notification();

        new TransactionTemplate(transactionManager).executeWithoutResult(status ->
                applicationEventPublisher.publishEvent(new NotificationCreatedEvent(notification))
        );

        verify(notificationRealtimePublisher).publish(NotificationRealtimeMessage.created(notification));
    }

    @Test
    void doesNotPublishRealtimeMessageAfterTransactionRollback() {
        NotificationResponse notification = notification();

        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            applicationEventPublisher.publishEvent(new NotificationCreatedEvent(notification));
            status.setRollbackOnly();
        });

        verifyNoInteractions(notificationRealtimePublisher);
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
