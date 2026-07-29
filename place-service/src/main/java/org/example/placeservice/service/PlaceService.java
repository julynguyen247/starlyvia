package org.example.placeservice.service;

import lombok.RequiredArgsConstructor;
import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.dto.PlaceSuggestionResponse;
import org.example.placeservice.provider.GeoapifyPlacesClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final GeoapifyPlacesClient geoapifyPlacesClient;

    @Cacheable(
            cacheNames = "geoapify-autocomplete",
            key = "@placeCacheKey.autocomplete(#query, #latitude, #longitude, #limit)"
    )
    public List<PlaceSuggestionResponse> autocomplete(
            String query,
            Double latitude,
            Double longitude,
            Integer limit,
            String sessionToken
    ) {
        if ((latitude == null) != (longitude == null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Latitude and longitude must be supplied together"
            );
        }
        return geoapifyPlacesClient.autocomplete(query, latitude, longitude, limit, sessionToken);
    }

    @Cacheable(
            cacheNames = "geoapify-nearby",
            key = "@placeCacheKey.nearby(#latitude, #longitude, #type, #radiusMeters, #limit)"
    )
    public List<PlaceDetailsResponse> nearby(
            Double latitude,
            Double longitude,
            String type,
            Integer radiusMeters,
            Integer limit
    ) {
        return geoapifyPlacesClient.nearby(latitude, longitude, type, radiusMeters, limit);
    }

    @Cacheable(
            cacheNames = "geoapify-nearby",
            key = "@placeCacheKey.viewport(#west, #south, #east, #north, #type, #limit)"
    )
    public List<PlaceDetailsResponse> viewport(
            Double west,
            Double south,
            Double east,
            Double north,
            String type,
            Integer limit
    ) {
        if (west >= east || south >= north) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid viewport bounds");
        }
        return geoapifyPlacesClient.viewport(west, south, east, north, type, limit);
    }

    @Cacheable(
            cacheNames = "geoapify-place-details",
            key = "@placeCacheKey.details(#provider, #providerPlaceId)"
    )
    public PlaceDetailsResponse details(PlaceProvider provider, String providerPlaceId) {
        if (provider == PlaceProvider.GEOAPIFY) {
            return geoapifyPlacesClient.details(providerPlaceId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported place provider");
    }
}
