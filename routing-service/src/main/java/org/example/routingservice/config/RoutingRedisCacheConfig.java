package org.example.routingservice.config;

import lombok.extern.slf4j.Slf4j;
import org.example.routingservice.dto.ComputeRouteResponse;
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
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Slf4j
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "app.cache.redis.enabled", havingValue = "true", matchIfMissing = true)
public class RoutingRedisCacheConfig implements CachingConfigurer {
    static final String ROUTE_CACHE = "openrouteservice-routes";

    private final RedisConnectionFactory connectionFactory;
    private final ObjectMapper objectMapper;
    private final Duration routeTtl;

    public RoutingRedisCacheConfig(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper,
            @Value("${app.cache.redis.route-ttl:30m}") Duration routeTtl
    ) {
        this.connectionFactory = connectionFactory;
        this.objectMapper = objectMapper;
        this.routeTtl = routeTtl;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        RedisCacheConfiguration routeCache = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues()
                .entryTtl(routeTtl)
                .computePrefixWith(cacheName -> "starlyvia:routing:v1:" + cacheName + "::")
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new JacksonJsonRedisSerializer<>(objectMapper, ComputeRouteResponse.class)
                ));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(routeCache)
                .withCacheConfiguration(ROUTE_CACHE, routeCache)
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

    private void logFailure(String operation, Cache cache, RuntimeException exception) {
        log.warn(
                "Redis cache {} failed for {}; continuing with OpenRouteService",
                operation,
                cache.getName(),
                exception
        );
    }
}
