package org.example.placeservice.service;

import org.example.placeservice.config.PlaceCacheKey;
import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.dto.PlaceSuggestionResponse;
import org.example.placeservice.provider.GeoapifyPlacesClient;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = PlaceServiceCacheTests.CacheTestConfig.class)
class PlaceServiceCacheTests {
    @Autowired
    private PlaceService placeService;

    @Autowired
    private GeoapifyPlacesClient geoapifyPlacesClient;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void resetCacheAndProvider() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
        reset(geoapifyPlacesClient);
    }

    @Test
    void cachesPlaceDetailsByProviderAndPlaceId() {
        PlaceDetailsResponse details = details("place-1");
        when(geoapifyPlacesClient.details("place-1")).thenReturn(details);

        PlaceDetailsResponse first = placeService.details(PlaceProvider.GEOAPIFY, "place-1");
        PlaceDetailsResponse second = placeService.details(PlaceProvider.GEOAPIFY, "place-1");

        assertThat(first).isEqualTo(details);
        assertThat(second).isEqualTo(details);
        verify(geoapifyPlacesClient, times(1)).details("place-1");
    }

    @Test
    void reusesAutocompleteAcrossSessionsForTheSameProviderInputs() {
        List<PlaceSuggestionResponse> suggestions = List.of(new PlaceSuggestionResponse(
                PlaceProvider.GEOAPIFY,
                "place-1",
                "Museum One",
                "Ho Chi Minh City",
                "Museum One, Ho Chi Minh City"
        ));
        when(geoapifyPlacesClient.autocomplete("museum", 10.77, 106.69, 5, "session-a"))
                .thenReturn(suggestions);

        List<PlaceSuggestionResponse> first = placeService.autocomplete(
                "museum", 10.77, 106.69, 5, "session-a"
        );
        List<PlaceSuggestionResponse> second = placeService.autocomplete(
                "museum", 10.77, 106.69, 5, "session-b"
        );

        assertThat(first).isEqualTo(suggestions);
        assertThat(second).isEqualTo(suggestions);
        verify(geoapifyPlacesClient, times(1))
                .autocomplete("museum", 10.77, 106.69, 5, "session-a");
    }

    private PlaceDetailsResponse details(String providerPlaceId) {
        return new PlaceDetailsResponse(
                PlaceProvider.GEOAPIFY,
                providerPlaceId,
                "Museum One",
                "1 Main Street",
                10.77,
                106.69,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Configuration(proxyBeanMethods = false)
    @EnableCaching
    static class CacheTestConfig {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager(
                    "geoapify-autocomplete",
                    "geoapify-nearby",
                    "geoapify-place-details"
            );
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
