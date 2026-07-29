package org.example.placeservice.provider;

import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceSuggestionResponse;

import java.util.List;

public interface PlaceProviderClient {
    List<PlaceSuggestionResponse> autocomplete(String query, Double latitude, Double longitude, Integer limit, String sessionToken);

    List<PlaceDetailsResponse> nearby(Double latitude, Double longitude, String type, Integer radiusMeters, Integer limit);

    List<PlaceDetailsResponse> viewport(
            Double west,
            Double south,
            Double east,
            Double north,
            String type,
            Integer limit
    );

    PlaceDetailsResponse details(String providerPlaceId);
}
