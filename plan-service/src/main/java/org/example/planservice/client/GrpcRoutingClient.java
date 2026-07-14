package org.example.planservice.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.example.planservice.dto.ComputeRouteResponse;
import org.example.planservice.dto.RouteCoordinateResponse;
import org.example.planservice.dto.RouteLegResponse;
import org.example.planservice.entity.PlanStop;
import org.example.planservice.grpc.routing.ComputeRouteRequest;
import org.example.planservice.grpc.routing.RouteCalculatorServiceGrpc;
import org.example.planservice.grpc.routing.RouteStop;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class GrpcRoutingClient implements RoutingClient {
    private final RouteCalculatorServiceGrpc.RouteCalculatorServiceBlockingStub routingServiceBlockingStub;

    @Override
    public ComputeRouteResponse computeRoute(TravelMode travelMode, List<PlanStop> stops) {
        ComputeRouteRequest request = ComputeRouteRequest.newBuilder()
                .setTravelMode(toGrpcTravelMode(travelMode))
                .addAllStops(stops.stream()
                        .map(this::toGrpcStop)
                        .toList())
                .build();

        try {
            org.example.planservice.grpc.routing.ComputeRouteResponse response =
                    routingServiceBlockingStub
                            .withDeadlineAfter(12, TimeUnit.SECONDS)
                            .computeRoute(request);
            return toResponse(response);
        } catch (StatusRuntimeException ex) {
            throw mapGrpcError(ex);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Routing service returned an invalid response",
                    ex
            );
        }
    }

    private RouteStop toGrpcStop(PlanStop stop) {
        RouteStop.Builder builder = RouteStop.newBuilder()
                .setLatitude(stop.getLatitude())
                .setLongitude(stop.getLongitude());
        if (stop.getId() != null) {
            builder.setStopId(stop.getId().toString());
        }
        return builder.build();
    }

    private org.example.planservice.grpc.routing.TravelMode toGrpcTravelMode(TravelMode travelMode) {
        return switch (travelMode) {
            case DRIVE -> org.example.planservice.grpc.routing.TravelMode.DRIVE;
            case WALK -> org.example.planservice.grpc.routing.TravelMode.WALK;
            case BICYCLE -> org.example.planservice.grpc.routing.TravelMode.BICYCLE;
        };
    }

    private ComputeRouteResponse toResponse(
            org.example.planservice.grpc.routing.ComputeRouteResponse response
    ) {
        return new ComputeRouteResponse(
                response.getProvider(),
                toTravelMode(response.getTravelMode()),
                response.getDistanceMeters(),
                response.getDurationSeconds(),
                response.getGeometryList().stream()
                        .map(coordinate -> new RouteCoordinateResponse(
                                coordinate.getLatitude(),
                                coordinate.getLongitude()
                        ))
                        .toList(),
                response.getLegsList().stream()
                        .map(leg -> new RouteLegResponse(
                                leg.getFromStopIndex(),
                                leg.getToStopIndex(),
                                parseOptionalUuid(leg.getFromStopId()),
                                parseOptionalUuid(leg.getToStopId()),
                                leg.getDistanceMeters(),
                                leg.getDurationSeconds()
                        ))
                        .toList()
        );
    }

    private TravelMode toTravelMode(org.example.planservice.grpc.routing.TravelMode travelMode) {
        return switch (travelMode) {
            case DRIVE -> TravelMode.DRIVE;
            case WALK -> TravelMode.WALK;
            case BICYCLE -> TravelMode.BICYCLE;
            case TRAVEL_MODE_UNSPECIFIED, UNRECOGNIZED ->
                    throw new IllegalArgumentException("Unknown travel mode");
        };
    }

    private UUID parseOptionalUuid(String rawValue) {
        return rawValue == null || rawValue.isBlank() ? null : UUID.fromString(rawValue);
    }

    private ResponseStatusException mapGrpcError(StatusRuntimeException ex) {
        Status.Code code = ex.getStatus().getCode();
        HttpStatus status = switch (code) {
            case INVALID_ARGUMENT -> HttpStatus.BAD_REQUEST;
            case FAILED_PRECONDITION -> HttpStatus.UNPROCESSABLE_CONTENT;
            case RESOURCE_EXHAUSTED -> HttpStatus.TOO_MANY_REQUESTS;
            case UNAVAILABLE, DEADLINE_EXCEEDED -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.BAD_GATEWAY;
        };
        String description = ex.getStatus().getDescription();
        String message = description == null || description.isBlank()
                ? "Routing service request failed"
                : description;
        return new ResponseStatusException(status, message, ex);
    }
}
