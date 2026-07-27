package org.example.notificationservice.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationHandshakeInterceptorTests {
    private final NotificationHandshakeInterceptor interceptor = new NotificationHandshakeInterceptor();

    @Test
    void acceptsGatewayAuthenticatedUser() {
        UUID userId = UUID.randomUUID();
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getHeaders()).thenReturn(headers);
        Map<String, Object> attributes = new HashMap<>();

        boolean accepted = interceptor.beforeHandshake(
                request,
                mock(ServerHttpResponse.class),
                mock(WebSocketHandler.class),
                attributes
        );

        assertThat(accepted).isTrue();
        assertThat(attributes).containsEntry(NotificationHandshakeInterceptor.USER_ID_ATTRIBUTE, userId);
    }

    @Test
    void rejectsHandshakeWithoutGatewayIdentity() {
        ServerHttpRequest request = mock(ServerHttpRequest.class);
        when(request.getHeaders()).thenReturn(new HttpHeaders());
        ServerHttpResponse response = mock(ServerHttpResponse.class);

        boolean accepted = interceptor.beforeHandshake(
                request,
                response,
                mock(WebSocketHandler.class),
                new HashMap<>()
        );

        assertThat(accepted).isFalse();
        verify(response).setStatusCode(HttpStatus.UNAUTHORIZED);
    }
}
