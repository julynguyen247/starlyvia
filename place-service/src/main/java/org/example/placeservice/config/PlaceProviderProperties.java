package org.example.placeservice.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "place.provider")
public class PlaceProviderProperties {
    @Valid
    private Geoapify geoapify = new Geoapify();

    @Getter
    @Setter
    public static class Geoapify {
        @NotBlank
        private String apiKey;
        @NotBlank
        private String baseUrl = "https://api.geoapify.com";
        @NotBlank
        private String nearbyCategories = "accommodation,catering,commercial,entertainment,leisure,tourism";
    }
}
