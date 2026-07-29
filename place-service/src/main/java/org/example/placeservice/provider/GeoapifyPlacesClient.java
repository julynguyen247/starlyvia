package org.example.placeservice.provider;

import lombok.RequiredArgsConstructor;
import org.example.placeservice.config.PlaceProviderProperties;
import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.dto.PlaceSuggestionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GeoapifyPlacesClient implements PlaceProviderClient {
    private static final int DEFAULT_LIMIT = 8;
    private static final int DEFAULT_VIEWPORT_LIMIT = 80;
    private static final int DEFAULT_RADIUS_METERS = 5_000;
    private static final List<String> VIEWPORT_CATEGORY_GROUPS = List.of(
            "catering,commercial",
            "tourism,entertainment,leisure,sport,religion",
            "service,education,healthcare,public_transport,parking,rental,childcare",
            "accommodation"
    );
    private final RestClient restClient;
    private final PlaceProviderProperties properties;

    @Override
    public List<PlaceSuggestionResponse> autocomplete(
            String query,
            Double latitude,
            Double longitude,
            Integer limit,
            String sessionToken
    ) {
        assertConfigured();

        UriComponentsBuilder uri = endpoint("/v1/geocode/autocomplete")
                .queryParam("text", query)
                .queryParam("format", "geojson")
                .queryParam("limit", resolvedLimit(limit));
        if (latitude != null && longitude != null) {
            uri.queryParam("bias", "proximity:" + longitude + "," + latitude);
        }

        JsonNode response = get(uri);
        List<PlaceSuggestionResponse> suggestions = new ArrayList<>();
        for (JsonNode feature : features(response)) {
            JsonNode place = feature.path("properties");
            String placeId = text(place, "place_id");
            if (!StringUtils.hasText(placeId)) {
                continue;
            }
            String name = firstText(place, "name", "address_line1", "formatted");
            String address = firstText(place, "address_line2", "formatted");
            suggestions.add(new PlaceSuggestionResponse(
                    PlaceProvider.GEOAPIFY,
                    placeId,
                    name,
                    address,
                    text(place, "formatted")
            ));
        }
        return suggestions;
    }

    @Override
    public List<PlaceDetailsResponse> nearby(
            Double latitude,
            Double longitude,
            String type,
            Integer radiusMeters,
            Integer limit
    ) {
        assertConfigured();

        int radius = radiusMeters == null ? DEFAULT_RADIUS_METERS : radiusMeters;
        String categories = StringUtils.hasText(type)
                ? type
                : properties.getGeoapify().getNearbyCategories();
        UriComponentsBuilder uri = endpoint("/v2/places")
                .queryParam("categories", categories)
                .queryParam("filter", "circle:" + longitude + "," + latitude + "," + radius)
                .queryParam("bias", "proximity:" + longitude + "," + latitude)
                .queryParam("limit", resolvedLimit(limit));

        JsonNode response = get(uri);
        List<PlaceDetailsResponse> places = new ArrayList<>();
        for (JsonNode feature : features(response)) {
            JsonNode place = feature.path("properties");
            String placeId = text(place, "place_id");
            String name = firstText(place, "name", "address_line1");
            if (!StringUtils.hasText(placeId) || !StringUtils.hasText(name)) {
                continue;
            }
            places.add(toDetails(place, placeId));
        }
        return places;
    }

    @Override
    public List<PlaceDetailsResponse> viewport(
            Double west,
            Double south,
            Double east,
            Double north,
            String type,
            Integer limit
    ) {
        assertConfigured();

        double centerLongitude = (west + east) / 2;
        double centerLatitude = (south + north) / 2;
        int resultLimit = resolvedViewportLimit(limit);
        List<String> categoryGroups = StringUtils.hasText(type)
                ? List.of(type)
                : VIEWPORT_CATEGORY_GROUPS;
        int groupLimit = Math.max(1, (int) Math.ceil((double) resultLimit / categoryGroups.size()));
        Map<String, PlaceDetailsResponse> places = new LinkedHashMap<>();

        for (String categories : categoryGroups) {
            UriComponentsBuilder uri = endpoint("/v2/places")
                    .queryParam("categories", categories)
                    .queryParam("filter", "rect:" + west + "," + north + "," + east + "," + south)
                    .queryParam("bias", "proximity:" + centerLongitude + "," + centerLatitude)
                    .queryParam("limit", StringUtils.hasText(type) ? resultLimit : groupLimit);
            for (PlaceDetailsResponse place : mapPlaces(get(uri))) {
                places.putIfAbsent(place.providerPlaceId(), place);
            }
        }

        return places.values().stream().limit(resultLimit).toList();
    }

    @Override
    public PlaceDetailsResponse details(String providerPlaceId) {
        assertConfigured();

        JsonNode response = get(endpoint("/v2/place-details")
                .queryParam("id", providerPlaceId)
                .queryParam("features", "details"));
        for (JsonNode feature : features(response)) {
            JsonNode place = feature.path("properties");
            if ("details".equals(text(place, "feature_type"))) {
                return toDetails(place, providerPlaceId);
            }
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
    }

    private PlaceDetailsResponse toDetails(JsonNode place, String providerPlaceId) {
        return new PlaceDetailsResponse(
                PlaceProvider.GEOAPIFY,
                providerPlaceId,
                firstText(place, "name", "address_line1", "formatted"),
                firstText(place, "formatted", "address_line2"),
                doubleValue(place, "lat"),
                doubleValue(place, "lon"),
                text(place.path("wiki_and_media"), "image"),
                null,
                null,
                text(place, "website"),
                text(place.path("contact"), "phone"),
                stringValues(place, "categories")
        );
    }

    private List<PlaceDetailsResponse> mapPlaces(JsonNode response) {
        List<PlaceDetailsResponse> places = new ArrayList<>();
        for (JsonNode feature : features(response)) {
            JsonNode place = feature.path("properties");
            String placeId = text(place, "place_id");
            String name = firstText(place, "name", "address_line1");
            if (StringUtils.hasText(placeId) && StringUtils.hasText(name)) {
                places.add(toDetails(place, placeId));
            }
        }
        return places;
    }

    private JsonNode get(UriComponentsBuilder uri) {
        URI requestUri = uri
                .queryParam("apiKey", properties.getGeoapify().getApiKey())
                .build()
                .encode()
                .toUri();
        try {
            JsonNode response = restClient.get()
                    .uri(requestUri)
                    .retrieve()
                    .onStatus(status -> status.isError(), (request, providerResponse) -> {
                        if (providerResponse.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
                        }
                        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Place provider request failed");
                    })
                    .body(JsonNode.class);
            return response == null ? null : response;
        } catch (ResourceAccessException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Place provider is unavailable");
        }
    }

    private UriComponentsBuilder endpoint(String path) {
        return UriComponentsBuilder.fromUriString(properties.getGeoapify().getBaseUrl()).path(path);
    }

    private Iterable<JsonNode> features(JsonNode response) {
        if (response == null) {
            return List.of();
        }
        JsonNode features = response.path("features");
        return features.isArray() ? features : List.of();
    }

    private int resolvedLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        return Math.max(1, Math.min(limit, 10));
    }

    private int resolvedViewportLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_VIEWPORT_LIMIT;
        }
        return Math.max(1, Math.min(limit, 100));
    }

    private void assertConfigured() {
        if (!StringUtils.hasText(properties.getGeoapify().getApiKey())) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Geoapify API key is not configured");
        }
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String value = text(node, fieldName);
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private Double doubleValue(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isNumber() ? value.asDouble() : null;
    }

    private List<String> stringValues(JsonNode node, String fieldName) {
        JsonNode values = node.path(fieldName);
        if (!values.isArray()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (JsonNode value : values) {
            if (value.isString() && StringUtils.hasText(value.asText())) {
                result.add(value.asText());
            }
        }
        return List.copyOf(result);
    }
}
