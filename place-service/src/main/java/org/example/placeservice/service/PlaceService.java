package org.example.placeservice.service;

import lombok.RequiredArgsConstructor;
import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.dto.PlaceSuggestionResponse;
import org.example.placeservice.provider.GooglePlacesClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final GooglePlacesClient googlePlacesClient;

    public List<PlaceSuggestionResponse> autocomplete(String query, Double latitude, Double longitude, Integer limit, String sessionToken) {
        if ((latitude == null) != (longitude == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude and longitude must be supplied together"
            );
        }
        return googlePlacesClient.autocomplete(query, latitude, longitude, limit, sessionToken);
    }

    public List<PlaceDetailsResponse> nearby(Double latitude, Double longitude, String type, Integer radiusMeters, Integer limit) {
        return googlePlacesClient.nearby(latitude, longitude, type, radiusMeters, limit);
    }

    public PlaceDetailsResponse details(PlaceProvider provider, String providerPlaceId) {
        if (provider == PlaceProvider.GOOGLE) {
            return googlePlacesClient.details(providerPlaceId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported place provider");
    }
}
