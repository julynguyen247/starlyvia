package org.example.planservice.dto;

public record RouteStepResponse(
        int instructionType,
        String instruction,
        String roadName,
        long distanceMeters,
        long durationSeconds,
        int geometryStartIndex,
        int geometryEndIndex
) {
}
