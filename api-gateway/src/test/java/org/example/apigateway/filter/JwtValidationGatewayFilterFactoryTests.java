package org.example.apigateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.example.apigateway.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class JwtValidationGatewayFilterFactoryTests {
    private static final String SECRET = "test-secret-key-test-secret-key-32-bytes";

    private final JwtValidationGatewayFilterFactory factory =
            new JwtValidationGatewayFilterFactory(new JwtUtil(SECRET));

    @Test
    void allowsLoginWithoutToken() {
        MockServerWebExchange exchange = exchange("/api/v1/auth/login", null);

        StepVerifier.create(filter().filter(exchange, chainExchange -> Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void rejectsProtectedPathWithoutToken() {
        MockServerWebExchange exchange = exchange("/api/v1/auth/me", null);
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();

        StepVerifier.create(filter().filter(exchange, chainExchange -> {
                    forwardedExchange.set(chainExchange);
                    return Mono.empty();
                }))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(forwardedExchange).hasValue(null);
    }

    @Test
    void rejectsPathThatOnlyStartsWithPublicAuthPath() {
        MockServerWebExchange exchange = exchange("/api/v1/auth/login-extra", null);

        StepVerifier.create(filter().filter(exchange, chainExchange -> Mono.empty()))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void allowsCorsPreflightWithoutToken() {
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.options("/api/v1/auth/me")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
        );
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();

        StepVerifier.create(filter().filter(exchange, chainExchange -> {
                    forwardedExchange.set(chainExchange);
                    return Mono.empty();
                }))
                .verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isNull();
        assertThat(forwardedExchange.get()).isNotNull();
    }

    @Test
    void forwardsProtectedPathWithUserHeadersWhenTokenIsValid() {
        MockServerWebExchange exchange = exchange("/api/v1/auth/me", token());
        AtomicReference<ServerWebExchange> forwardedExchange = new AtomicReference<>();

        StepVerifier.create(filter().filter(exchange, chainExchange -> {
                    forwardedExchange.set(chainExchange);
                    return Mono.empty();
                }))
                .verifyComplete();

        assertThat(forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Email"))
                .isEqualTo("user@example.com");
        assertThat(forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Id"))
                .isEqualTo("7f4b0f70-2e72-4d43-9bb8-9bf61ab56319");
        assertThat(forwardedExchange.get().getRequest().getHeaders().getFirst("X-User-Role"))
                .isEqualTo("USER");
    }

    private GatewayFilter filter() {
        return factory.apply(new JwtValidationGatewayFilterFactory.Config());
    }

    private MockServerWebExchange exchange(String path, String token) {
        MockServerHttpRequest.BaseBuilder<?> request = MockServerHttpRequest.get(path);
        if (token != null) {
            request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }
        return MockServerWebExchange.from(request);
    }

    private String token() {
        return Jwts.builder()
                .subject("user@example.com")
                .claim("userId", "7f4b0f70-2e72-4d43-9bb8-9bf61ab56319")
                .claim("role", "USER")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
