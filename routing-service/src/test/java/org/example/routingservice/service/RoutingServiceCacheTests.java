package org.example.routingservice.service;

import org.example.routingservice.config.RouteCacheKey;
import org.example.routingservice.dto.ComputeRouteRequest;
import org.example.routingservice.dto.ComputeRouteResponse;
import org.example.routingservice.dto.RouteCoordinateResponse;
import org.example.routingservice.dto.RouteLegResponse;
import org.example.routingservice.dto.RouteStopRequest;
import org.example.routingservice.dto.TravelMode;
import org.example.routingservice.provider.RoutingProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = RoutingServiceCacheTests.CacheTestConfig.class)
class RoutingServiceCacheTests {
    @Autowired
    private RoutingService routingService;

    @Autowired
    private RoutingProvider routingProvider;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void resetCacheAndProvider() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        reset(routingProvider);
    }

    @Test
    void cachesRouteForIdenticalModeAndOrderedStops() {
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
                List.of(new RouteLegResponse(0, 1, firstStopId, secondStopId, 1521, 420, List.of()))
        );
        when(routingProvider.computeRoute(request)).thenReturn(response);

        ComputeRouteResponse first = routingService.computeRoute(request);
        ComputeRouteResponse second = routingService.computeRoute(request);

        assertThat(first).isEqualTo(response);
        assertThat(second).isEqualTo(response);
        verify(routingProvider, times(1)).computeRoute(request);
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("openrouteservice-routes");
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
