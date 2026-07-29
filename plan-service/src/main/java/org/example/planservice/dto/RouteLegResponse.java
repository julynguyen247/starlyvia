package org.example.planservice.dto;

import java.util.List;
import java.util.UUID;

public record RouteLegResponse(
        int fromStopIndex,
        int toStopIndex,
        UUID fromStopId,
        UUID toStopId,
        long distanceMeters,
        long durationSeconds,
        List<RouteStepResponse> steps
) {
}
