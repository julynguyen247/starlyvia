package org.example.placeservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "place.provider")
public class PlaceProviderProperties {
    private Google google = new Google();

    @Getter
    @Setter
    public static class Google {
        private String apiKey;
        private String baseUrl = "https://places.googleapis.com/v1";
    }
}
