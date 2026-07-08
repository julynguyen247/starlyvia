package org.example.placeservice;

import org.example.placeservice.config.PlaceProviderProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PlaceProviderProperties.class)
public class PlaceServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlaceServiceApplication.class, args);
    }
}
