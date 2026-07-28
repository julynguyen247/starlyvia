package org.example.routingservice.config;

import org.example.routingservice.dto.ComputeRouteRequest;
import org.example.routingservice.dto.ComputeRouteResponse;
import org.example.routingservice.dto.RouteCoordinateResponse;
import org.example.routingservice.dto.RouteLegResponse;
import org.example.routingservice.dto.RouteStopRequest;
import org.example.routingservice.dto.TravelMode;
import org.example.routingservice.provider.RoutingProvider;
import org.example.routingservice.service.RoutingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.convert.ApplicationConversionService;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RoutingRedisCacheFailureTests.CacheTestConfig.class)
@TestPropertySource(properties = "app.cache.redis.enabled=true")
class RoutingRedisCacheFailureTests {
    @Autowired
    private RoutingService routingService;

    @Autowired
    private RoutingProvider routingProvider;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void fallsBackToOpenRouteServiceWhenRedisIsUnavailable() {
        UUID firstStopId = UUID.randomUUID();
        UUID secondStopId = UUID.randomUUID();
        ComputeRouteRequest request = new ComputeRouteRequest(
                TravelMode.DRIVE,
                List.of(
                        new RouteStopRequest(firstStopId, 10.77, 106.69),
                        new RouteStopRequest(secondStopId, 10.78, 106.70)
                )
        );
        ComputeRouteResponse response = new ComputeRouteResponse(
                "OPENROUTESERVICE",
                TravelMode.DRIVE,
                1521,
                420,
                List.of(
                        new RouteCoordinateResponse(10.77, 106.69),
                        new RouteCoordinateResponse(10.78, 106.70)
                ),
                List.of(new RouteLegResponse(0, 1, firstStopId, secondStopId, 1521, 420))
        );
        when(routingProvider.computeRoute(request)).thenReturn(response);

        assertThat(routingService.computeRoute(request)).isEqualTo(response);
        verify(routingProvider).computeRoute(request);
    }

    @Test
    void configuresTheRouteCacheTtl() {
        RedisCacheManager redisCacheManager = (RedisCacheManager) cacheManager;
        RedisCacheConfiguration configuration = redisCacheManager.getCacheConfigurations()
                .get(RoutingRedisCacheConfig.ROUTE_CACHE);

        assertThat(configuration.getTtlFunction().getTimeToLive("key", "value"))
                .isEqualTo(Duration.ofMinutes(30));
    }

    @Configuration
    @Import(RoutingRedisCacheConfig.class)
    static class CacheTestConfig {
        @Bean
        static ConversionService conversionService() {
            return ApplicationConversionService.getSharedInstance();
        }

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
            when(connectionFactory.getConnection())
                    .thenThrow(new RedisConnectionFailureException("Redis is unavailable"));
            return connectionFactory;
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        RouteCacheKey routeCacheKey() {
            return new RouteCacheKey();
        }

        @Bean
        RoutingProvider routingProvider() {
            return mock(RoutingProvider.class);
        }

        @Bean
        RoutingService routingService(RoutingProvider routingProvider) {
            return new RoutingService(routingProvider);
        }
    }
}
