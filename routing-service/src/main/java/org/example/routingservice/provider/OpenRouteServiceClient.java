package org.example.routingservice.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.routingservice.config.OpenRouteServiceProperties;
import org.example.routingservice.dto.ComputeRouteRequest;
import org.example.routingservice.dto.ComputeRouteResponse;
import org.example.routingservice.dto.RouteCoordinateResponse;
import org.example.routingservice.dto.RouteLegResponse;
import org.example.routingservice.dto.RouteStopRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenRouteServiceClient implements RoutingProvider {
    private static final String PROVIDER_NAME = "OPENROUTESERVICE";

    private final RestClient routingRestClient;
    private final OpenRouteServiceProperties properties;

    @Override
    public ComputeRouteResponse computeRoute(ComputeRouteRequest request) {
        assertConfigured();

        try {
            JsonNode response = routingRestClient.post()
                    .uri(normalizedBaseUrl() + "/v2/directions/"
                            + request.travelMode().providerProfile() + "/geojson")
                    .header("Authorization", properties.apiKey())
                    .body(requestBody(request))
                    .retrieve()
                    .body(JsonNode.class);
            return toResponse(response, request);
        } catch (RestClientResponseException ex) {
            throw mapProviderError(ex);
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Routing provider is unavailable",
                    ex
            );
        }
    }

    private Map<String, Object> requestBody(ComputeRouteRequest request) {
        List<List<Double>> coordinates = request.stops().stream()
                .map(stop -> List.of(stop.longitude(), stop.latitude()))
                .toList();
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("coordinates", coordinates);
        body.put("instructions", false);
        return body;
    }

    private ComputeRouteResponse toResponse(JsonNode response, ComputeRouteRequest request) {
        JsonNode feature = response == null ? null : response.path("features").path(0);
        if (feature == null || feature.isMissingNode()) {
            throw invalidProviderResponse();
        }

        JsonNode propertiesNode = feature.path("properties");
        JsonNode summary = propertiesNode.path("summary");
        List<RouteCoordinateResponse> geometry = parseGeometry(feature.path("geometry").path("coordinates"));
        List<RouteLegResponse> legs = parseLegs(propertiesNode.path("segments"), request.stops());

        return new ComputeRouteResponse(
                PROVIDER_NAME,
                request.travelMode(),
                roundedNumber(summary, "distance"),
                roundedNumber(summary, "duration"),
                geometry,
                legs
        );
    }

    private List<RouteCoordinateResponse> parseGeometry(JsonNode coordinatesNode) {
        if (!coordinatesNode.isArray() || coordinatesNode.isEmpty()) {
            throw invalidProviderResponse();
        }

        List<RouteCoordinateResponse> geometry = new ArrayList<>();
        for (JsonNode coordinate : coordinatesNode) {
            if (!coordinate.isArray() || coordinate.size() < 2
                    || !coordinate.path(0).isNumber() || !coordinate.path(1).isNumber()) {
                throw invalidProviderResponse();
            }
            geometry.add(new RouteCoordinateResponse(
                    coordinate.path(1).asDouble(),
                    coordinate.path(0).asDouble()
            ));
        }
        return List.copyOf(geometry);
    }

    private List<RouteLegResponse> parseLegs(JsonNode segmentsNode, List<RouteStopRequest> stops) {
        if (!segmentsNode.isArray() || segmentsNode.size() != stops.size() - 1) {
            throw invalidProviderResponse();
        }

        List<RouteLegResponse> legs = new ArrayList<>();
        for (int index = 0; index < segmentsNode.size(); index++) {
            RouteStopRequest from = stops.get(index);
            RouteStopRequest to = stops.get(index + 1);
            JsonNode segment = segmentsNode.path(index);
            legs.add(new RouteLegResponse(
                    index,
                    index + 1,
                    from.stopId(),
                    to.stopId(),
                    roundedNumber(segment, "distance"),
                    roundedNumber(segment, "duration")
            ));
        }
        return List.copyOf(legs);
    }

    private long roundedNumber(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (!value.isNumber()) {
            throw invalidProviderResponse();
        }
        return Math.round(value.asDouble());
    }

    private ResponseStatusException mapProviderError(RestClientResponseException ex) {
        int status = ex.getStatusCode().value();
        if (status == 429) {
            return new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Routing provider quota exceeded",
                    ex
            );
        }
        if (status >= 400 && status < 500 && status != 401 && status != 403) {
            return new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_CONTENT,
                    "No route could be calculated for the supplied stops",
                    ex
            );
        }
        log.warn("OpenRouteService request failed with status {}", status);
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Routing provider rejected the request",
                ex
        );
    }

    private ResponseStatusException invalidProviderResponse() {
        return new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Routing provider returned an invalid response"
        );
    }

    private void assertConfigured() {
        if (!StringUtils.hasText(properties.apiKey())) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "OpenRouteService API key is not configured"
            );
        }
    }

    private String normalizedBaseUrl() {
        String baseUrl = properties.baseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return "https://api.openrouteservice.org";
        }
        return baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
    }
}
