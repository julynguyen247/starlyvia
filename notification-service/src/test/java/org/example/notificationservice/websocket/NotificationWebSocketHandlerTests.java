package org.example.notificationservice.websocket;

import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.NotificationStatus;
import org.example.notificationservice.entity.NotificationType;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationWebSocketHandlerTests {
    @Test
    void sendsNotificationOnlyToRecipientSessions() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"type\":\"NOTIFICATION_CREATED\"}");
        NotificationWebSocketHandler handler = new NotificationWebSocketHandler(objectMapper);
        UUID recipientId = UUID.randomUUID();
        WebSocketSession recipientSession = session("recipient-session", recipientId);
        WebSocketSession otherSession = session("other-session", UUID.randomUUID());
        handler.afterConnectionEstablished(recipientSession);
        handler.afterConnectionEstablished(otherSession);

        handler.sendToUser(recipientId, NotificationRealtimeMessage.created(notification(recipientId)));

        verify(recipientSession).sendMessage(any(TextMessage.class));
        verify(otherSession, never()).sendMessage(any(TextMessage.class));
    }

    private WebSocketSession session(String id, UUID userId) {
        WebSocketSession session = mock(WebSocketSession.class);
        when(session.getId()).thenReturn(id);
        when(session.isOpen()).thenReturn(true);
        when(session.getAttributes()).thenReturn(Map.of(
                NotificationHandshakeInterceptor.USER_ID_ATTRIBUTE,
                userId
        ));
        return session;
    }

    private NotificationResponse notification(UUID recipientId) {
        return new NotificationResponse(
                UUID.randomUUID(),
                recipientId,
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
