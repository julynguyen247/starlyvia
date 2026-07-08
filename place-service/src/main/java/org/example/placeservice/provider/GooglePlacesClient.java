package org.example.placeservice.provider;

import lombok.RequiredArgsConstructor;
import org.example.placeservice.config.PlaceProviderProperties;
import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.dto.PlaceSuggestionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GooglePlacesClient implements PlaceProviderClient {
    private static final int DEFAULT_LIMIT = 8;
    private static final int DEFAULT_RADIUS_METERS = 5_000;
    private static final String AUTOCOMPLETE_FIELD_MASK = String.join(",",
            "suggestions.placePrediction.placeId",
            "suggestions.placePrediction.text.text",
            "suggestions.placePrediction.structuredFormat.mainText.text",
            "suggestions.placePrediction.structuredFormat.secondaryText.text");
    private static final String DETAILS_FIELD_MASK = String.join(",",
            "id",
            "displayName.text",
            "formattedAddress",
            "location",
            "rating",
            "userRatingCount",
            "websiteUri",
            "nationalPhoneNumber");
    private static final String NEARBY_FIELD_MASK = String.join(",",
            "places.id",
            "places.displayName.text",
            "places.formattedAddress",
            "places.location",
            "places.rating",
            "places.userRatingCount",
            "places.websiteUri",
            "places.nationalPhoneNumber");

    private final RestClient restClient;
    private final PlaceProviderProperties properties;

    @Override
    public List<PlaceSuggestionResponse> autocomplete(String query, Double latitude, Double longitude, Integer limit, String sessionToken) {
        assertConfigured();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("input", query);
        body.put("includeQueryPredictions", false);
        if (StringUtils.hasText(sessionToken)) {
            body.put("sessionToken", sessionToken);
        }
        if (latitude != null && longitude != null) {
            body.put("locationBias", circle(latitude, longitude, DEFAULT_RADIUS_METERS));
        }

        JsonNode response = restClient.post()
                .uri(properties.getGoogle().getBaseUrl() + "/places:autocomplete")
                .header("X-Goog-Api-Key", properties.getGoogle().getApiKey())
                .header("X-Goog-FieldMask", AUTOCOMPLETE_FIELD_MASK)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        List<PlaceSuggestionResponse> suggestions = new ArrayList<>();
        JsonNode nodes = response == null ? null : response.path("suggestions");
        if (nodes != null && nodes.isArray()) {
            for (JsonNode node : nodes) {
                JsonNode prediction = node.path("placePrediction");
                if (prediction.isMissingNode()) {
                    continue;
                }
                suggestions.add(new PlaceSuggestionResponse(
                        PlaceProvider.GOOGLE,
                        text(prediction, "placeId"),
                        text(prediction.path("structuredFormat").path("mainText"), "text"),
                        text(prediction.path("structuredFormat").path("secondaryText"), "text"),
                        text(prediction.path("text"), "text")
                ));
                if (suggestions.size() >= resolvedLimit(limit)) {
                    break;
                }
            }
        }
        return suggestions;
    }

    @Override
    public List<PlaceDetailsResponse> nearby(Double latitude, Double longitude, String type, Integer radiusMeters, Integer limit) {
        assertConfigured();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("maxResultCount", resolvedLimit(limit));
        body.put("locationRestriction", circle(latitude, longitude, radiusMeters == null ? DEFAULT_RADIUS_METERS : radiusMeters));
        if (StringUtils.hasText(type)) {
            body.put("includedTypes", List.of(type));
        }

        JsonNode response = restClient.post()
                .uri(properties.getGoogle().getBaseUrl() + "/places:searchNearby")
                .header("X-Goog-Api-Key", properties.getGoogle().getApiKey())
                .header("X-Goog-FieldMask", NEARBY_FIELD_MASK)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        List<PlaceDetailsResponse> places = new ArrayList<>();
        JsonNode nodes = response == null ? null : response.path("places");
        if (nodes != null && nodes.isArray()) {
            for (JsonNode node : nodes) {
                places.add(toDetails(node));
            }
        }
        return places;
    }

    @Override
    public PlaceDetailsResponse details(String providerPlaceId) {
        assertConfigured();

        JsonNode response = restClient.get()
                .uri(properties.getGoogle().getBaseUrl() + "/places/" + normalizePlaceId(providerPlaceId))
                .header("X-Goog-Api-Key", properties.getGoogle().getApiKey())
                .header("X-Goog-FieldMask", DETAILS_FIELD_MASK)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || response.isMissingNode()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }
        return toDetails(response);
    }

    private PlaceDetailsResponse toDetails(JsonNode node) {
        JsonNode location = node.path("location");
        return new PlaceDetailsResponse(
                PlaceProvider.GOOGLE,
                text(node, "id"),
                text(node.path("displayName"), "text"),
                text(node, "formattedAddress"),
                doubleValue(location, "latitude"),
                doubleValue(location, "longitude"),
                null,
                doubleValue(node, "rating"),
                intValue(node, "userRatingCount"),
                text(node, "websiteUri"),
                text(node, "nationalPhoneNumber")
        );
    }

    private Map<String, Object> circle(Double latitude, Double longitude, Integer radiusMeters) {
        return Map.of(
                "circle", Map.of(
                        "center", Map.of(
                                "latitude", latitude,
                                "longitude", longitude
                        ),
                        "radius", radiusMeters
                )
        );
    }

    private int resolvedLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(1, Math.min(limit, 10));
    }

    private void assertConfigured() {
        if (!StringUtils.hasText(properties.getGoogle().getApiKey())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google Places API key is not configured");
        }
    }

    private String normalizePlaceId(String providerPlaceId) {
        if (providerPlaceId != null && providerPlaceId.startsWith("places/")) {
            return providerPlaceId.substring("places/".length());
        }
        return providerPlaceId;
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private Double doubleValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isNumber() ? value.asDouble() : null;
    }

    private Integer intValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isInt() ? value.asInt() : null;
    }
}
