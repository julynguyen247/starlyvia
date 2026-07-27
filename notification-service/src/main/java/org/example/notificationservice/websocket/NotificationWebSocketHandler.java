package org.example.notificationservice.websocket;

import lombok.extern.slf4j.Slf4j;
import org.example.notificationservice.dto.NotificationRealtimeMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    private static final int SEND_TIME_LIMIT_MILLIS = 10_000;
    private static final int BUFFER_SIZE_LIMIT_BYTES = 64 * 1024;

    private final ObjectMapper objectMapper;
    private final Map<UUID, Map<String, WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    public NotificationWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        UUID userId = userId(session);
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(
                session,
                SEND_TIME_LIMIT_MILLIS,
                BUFFER_SIZE_LIMIT_BYTES
        );
        sessionsByUser.computeIfAbsent(userId, ignored -> new ConcurrentHashMap<>())
                .put(session.getId(), safeSession);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        removeSession(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        removeSession(session);
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    public void sendToUser(UUID userId, NotificationRealtimeMessage message) {
        Map<String, WebSocketSession> userSessions = sessionsByUser.get(userId);
        if (userSessions == null || userSessions.isEmpty()) {
            return;
        }

        TextMessage payload;
        try {
            payload = new TextMessage(objectMapper.writeValueAsString(message));
        } catch (Exception exception) {
            log.error("Unable to serialize notification WebSocket message", exception);
            return;
        }
        userSessions.forEach((sessionId, session) -> {
            if (!session.isOpen()) {
                userSessions.remove(sessionId, session);
                return;
            }
            try {
                session.sendMessage(payload);
            } catch (Exception exception) {
                userSessions.remove(sessionId, session);
                log.warn("Unable to deliver notification over WebSocket session {}", sessionId, exception);
            }
        });
        if (userSessions.isEmpty()) {
            sessionsByUser.remove(userId, userSessions);
        }
    }

    private void removeSession(WebSocketSession session) {
        Object attribute = session.getAttributes().get(NotificationHandshakeInterceptor.USER_ID_ATTRIBUTE);
        if (!(attribute instanceof UUID userId)) {
            return;
        }
        Map<String, WebSocketSession> userSessions = sessionsByUser.get(userId);
        if (userSessions == null) {
            return;
        }
        userSessions.remove(session.getId());
        if (userSessions.isEmpty()) {
            sessionsByUser.remove(userId, userSessions);
        }
    }

    private UUID userId(WebSocketSession session) throws IOException {
        Object attribute = session.getAttributes().get(NotificationHandshakeInterceptor.USER_ID_ATTRIBUTE);
        if (attribute instanceof UUID userId) {
            return userId;
        }
        session.close(CloseStatus.POLICY_VIOLATION);
        throw new IOException("Authenticated user is missing from the WebSocket session");
    }
}
