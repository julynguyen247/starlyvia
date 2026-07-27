package org.example.notificationservice.websocket;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.example.notificationservice.event.NotificationCreatedEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationRealtimeEventListener {
    private final NotificationWebSocketHandler notificationWebSocketHandler;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onNotificationCreated(NotificationCreatedEvent event) {
        notificationWebSocketHandler.sendToUser(
                event.notification().recipientUserId(),
                NotificationRealtimeMessage.created(event.notification())
        );
    }
}
