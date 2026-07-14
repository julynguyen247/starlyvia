package org.example.routingservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "routing.provider.openrouteservice")
public record OpenRouteServiceProperties(
        String apiKey,
        String baseUrl
) {
}
