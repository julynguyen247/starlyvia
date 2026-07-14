package org.example.planservice.dto;

import org.example.planservice.client.TravelMode;

import java.util.List;

public record ComputeRouteResponse(
    String provider,
    TravelMode travelMode,
    long distanceMeters,
    long durationSeconds,
    List<RouteCoordinateResponse> geometry,
    List<RouteLegResponse> legs
) {
}
