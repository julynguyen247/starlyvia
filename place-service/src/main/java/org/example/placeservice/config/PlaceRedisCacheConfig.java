package org.example.placeservice.config;

import lombok.extern.slf4j.Slf4j;
import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceSuggestionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.List;

@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.cache.redis.enabled", havingValue = "true", matchIfMissing = true)
public class PlaceRedisCacheConfig implements CachingConfigurer {
    static final String AUTOCOMPLETE_CACHE = "geoapify-autocomplete";
    static final String NEARBY_CACHE = "geoapify-nearby";
    static final String DETAILS_CACHE = "geoapify-place-details";

    private final RedisConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;
    private final Duration autocompleteTtl;
    private final Duration nearbyTtl;
    private final Duration detailsTtl;

    public PlaceRedisCacheConfig(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            @Value("${app.cache.redis.autocomplete-ttl:2m}") Duration autocompleteTtl,
            @Value("${app.cache.redis.nearby-ttl:10m}") Duration nearbyTtl,
            @Value("${app.cache.redis.details-ttl:24h}") Duration detailsTtl
    ) {
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
        this.autocompleteTtl = autocompleteTtl;
        this.nearbyTtl = nearbyTtl;
        this.detailsTtl = detailsTtl;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        RedisCacheConfiguration base = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> "starlyvia:place:v1:" + cacheName + "::");

        RedisCacheConfiguration autocomplete = base
                .entryTtl(autocompleteTtl)
                .serializeValuesWith(serializationPair(listType(PlaceSuggestionResponse.class)));
        RedisCacheConfiguration nearby = base
                .entryTtl(nearbyTtl)
                .serializeValuesWith(serializationPair(listType(PlaceDetailsResponse.class)));
        RedisCacheConfiguration details = base
                .entryTtl(detailsTtl)
                .serializeValuesWith(serializationPair(PlaceDetailsResponse.class));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(details)
                .withCacheConfiguration(AUTOCOMPLETE_CACHE, autocomplete)
                .withCacheConfiguration(NEARBY_CACHE, nearby)
                .withCacheConfiguration(DETAILS_CACHE, details)
                .disableCreateOnMissingCache()
                .build();
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                logFailure("read", cache, exception);
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                logFailure("write", cache, exception);
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                logFailure("evict", cache, exception);
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                logFailure("clear", cache, exception);
            }
        };
    }

    private RedisSerializationContext.SerializationPair<?> serializationPair(Class<?> type) {
        return RedisSerializationContext.SerializationPair.fromSerializer(
                new JacksonJsonRedisSerializer<>(objectMapper, type)
        );
    }

    private RedisSerializationContext.SerializationPair<?> serializationPair(JavaType type) {
        return RedisSerializationContext.SerializationPair.fromSerializer(
                new JacksonJsonRedisSerializer<>(objectMapper, type)
        );
    }

    private JavaType listType(Class<?> elementType) {
        return objectMapper.getTypeFactory().constructCollectionType(List.class, elementType);
    }

    private void logFailure(String operation, Cache cache, RuntimeException exception) {
        log.warn(
                "Redis cache {} failed for {}; continuing with the Geoapify provider",
                operation,
                cache.getName(),
                exception
        );
    }
}
