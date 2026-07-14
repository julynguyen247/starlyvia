package org.example.routingservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ComputeRouteRequest(
        @NotNull TravelMode travelMode,
        @NotNull @Size(min = 2, max = 50) List<@Valid RouteStopRequest> stops
) {
}
