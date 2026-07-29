package org.example.placeservice.config;

import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.provider.GeoapifyPlacesClient;
import org.example.placeservice.service.PlaceService;
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

import java.util.List;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PlaceRedisCacheFailureTests.CacheTestConfig.class)
@TestPropertySource(properties = "app.cache.redis.enabled=true")
class PlaceRedisCacheFailureTests {
    @Autowired
    private PlaceService placeService;

    @Autowired
    private GeoapifyPlacesClient geoapifyPlacesClient;

    @Autowired
    private CacheManager cacheManager;

    @Test
    void fallsBackToGeoapifyWhenRedisIsUnavailable() {
        PlaceDetailsResponse details = new PlaceDetailsResponse(
                PlaceProvider.GEOAPIFY,
                "place-1",
                "Museum One",
                "1 Main Street",
                10.77,
                106.69,
                null,
                null,
                null,
                null,
                null,
                List.of()
        );
        when(geoapifyPlacesClient.details("place-1")).thenReturn(details);

        assertThat(placeService.details(PlaceProvider.GEOAPIFY, "place-1")).isEqualTo(details);
        verify(geoapifyPlacesClient).details("place-1");
    }

    @Test
    void configuresSeparateTtlsForEachGeoapifyCache() {
        RedisCacheManager redisCacheManager = (RedisCacheManager) cacheManager;

        assertThat(ttl(redisCacheManager, PlaceRedisCacheConfig.AUTOCOMPLETE_CACHE))
                .isEqualTo(Duration.ofMinutes(2));
        assertThat(ttl(redisCacheManager, PlaceRedisCacheConfig.NEARBY_CACHE))
                .isEqualTo(Duration.ofMinutes(10));
        assertThat(ttl(redisCacheManager, PlaceRedisCacheConfig.DETAILS_CACHE))
                .isEqualTo(Duration.ofHours(24));
    }

    private Duration ttl(RedisCacheManager cacheManager, String cacheName) {
        RedisCacheConfiguration configuration = cacheManager.getCacheConfigurations().get(cacheName);
        return configuration.getTtlFunction().getTimeToLive("key", "value");
    }

    @Configuration
    @Import(PlaceRedisCacheConfig.class)
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
        PlaceCacheKey placeCacheKey() {
            return new PlaceCacheKey();
        }

        @Bean
        GeoapifyPlacesClient geoapifyPlacesClient() {
            return mock(GeoapifyPlacesClient.class);
        }

        @Bean
        PlaceService placeService(GeoapifyPlacesClient geoapifyPlacesClient) {
            return new PlaceService(geoapifyPlacesClient);
        }
    }
}
