package org.example.placeservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "place.provider")
public class PlaceProviderProperties {
    private Geoapify geoapify = new Geoapify();

    @Getter
    @Setter
    public static class Geoapify {
        private String apiKey;
        private String baseUrl = "https://api.geoapify.com";
        private String nearbyCategories = "accommodation,catering,commercial,entertainment,leisure,tourism";
    }
}
