package org.example.notificationservice.websocket;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;
import java.util.UUID;

@Component
public class NotificationHandshakeInterceptor implements HandshakeInterceptor {
    static final String USER_ID_ATTRIBUTE = "notificationUserId";

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        String userIdHeader = request.getHeaders().getFirst("X-User-Id");
        if (userIdHeader == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }

        try {
            attributes.put(USER_ID_ATTRIBUTE, UUID.fromString(userIdHeader));
            return true;
        } catch (IllegalArgumentException exception) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
        // No resources are acquired during the handshake.
    }
}
