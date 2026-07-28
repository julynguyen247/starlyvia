package org.example.routingservice.grpcserver;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.example.routingservice.dto.RouteStopRequest;
import org.example.routingservice.grpc.routing.RouteCalculatorServiceGrpc;
import org.example.routingservice.grpc.routing.RouteCoordinate;
import org.example.routingservice.grpc.routing.RouteLeg;
import org.example.routingservice.grpc.routing.RouteStep;
import org.example.routingservice.grpc.routing.RouteStop;
import org.example.routingservice.service.RoutingService;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RouteCalculatorGrpcService extends RouteCalculatorServiceGrpc.RouteCalculatorServiceImplBase {
    private final RoutingService routingService;

    @Override
    public void computeRoute(
            org.example.routingservice.grpc.routing.ComputeRouteRequest request,
            StreamObserver<org.example.routingservice.grpc.routing.ComputeRouteResponse> responseObserver
    ) {
        try {
            org.example.routingservice.dto.ComputeRouteResponse response =
                    routingService.computeRoute(toInternalRequest(request));
            responseObserver.onNext(toGrpcResponse(response));
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(ex.getMessage())
                    .withCause(ex)
                    .asRuntimeException());
        } catch (ResponseStatusException ex) {
            responseObserver.onError(mapStatus(ex)
                    .withDescription(ex.getReason())
                    .withCause(ex)
                    .asRuntimeException());
        } catch (RuntimeException ex) {
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Failed to compute route")
                    .withCause(ex)
                    .asRuntimeException());
        }
    }

    private org.example.routingservice.dto.ComputeRouteRequest toInternalRequest(
            org.example.routingservice.grpc.routing.ComputeRouteRequest request
    ) {
        if (request.getStopsCount() < 2 || request.getStopsCount() > 50) {
            throw new IllegalArgumentException("stops must contain between 2 and 50 items");
        }

        return new org.example.routingservice.dto.ComputeRouteRequest(
                toInternalTravelMode(request.getTravelMode()),
                request.getStopsList().stream()
                        .map(this::toInternalStop)
                        .toList()
        );
    }

    private org.example.routingservice.dto.TravelMode toInternalTravelMode(
            org.example.routingservice.grpc.routing.TravelMode travelMode
    ) {
        return switch (travelMode) {
            case DRIVE -> org.example.routingservice.dto.TravelMode.DRIVE;
            case WALK -> org.example.routingservice.dto.TravelMode.WALK;
            case BICYCLE -> org.example.routingservice.dto.TravelMode.BICYCLE;
            case TRAVEL_MODE_UNSPECIFIED, UNRECOGNIZED ->
                    throw new IllegalArgumentException("travel_mode is required");
        };
    }

    private RouteStopRequest toInternalStop(RouteStop stop) {
        validateCoordinates(stop.getLatitude(), stop.getLongitude());
        return new RouteStopRequest(
                parseOptionalUuid(stop.getStopId()),
                stop.getLatitude(),
                stop.getLongitude()
        );
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }

    private UUID parseOptionalUuid(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(rawValue);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("stop_id must be a valid UUID", ex);
        }
    }

    private org.example.routingservice.grpc.routing.ComputeRouteResponse toGrpcResponse(
            org.example.routingservice.dto.ComputeRouteResponse response
    ) {
        return org.example.routingservice.grpc.routing.ComputeRouteResponse.newBuilder()
                .setProvider(response.provider())
                .setTravelMode(toGrpcTravelMode(response.travelMode()))
                .setDistanceMeters(response.distanceMeters())
                .setDurationSeconds(response.durationSeconds())
                .addAllGeometry(response.geometry().stream()
                        .map(coordinate -> RouteCoordinate.newBuilder()
                                .setLatitude(coordinate.latitude())
                                .setLongitude(coordinate.longitude())
                                .build())
                        .toList())
                .addAllLegs(response.legs().stream()
                        .map(leg -> {
                            RouteLeg.Builder builder = RouteLeg.newBuilder()
                                    .setFromStopIndex(leg.fromStopIndex())
                                    .setToStopIndex(leg.toStopIndex())
                                    .setDistanceMeters(leg.distanceMeters())
                                    .setDurationSeconds(leg.durationSeconds())
                                    .addAllSteps(leg.steps().stream()
                                            .map(step -> RouteStep.newBuilder()
                                                    .setInstructionType(step.instructionType())
                                                    .setInstruction(step.instruction())
                                                    .setRoadName(step.roadName())
                                                    .setDistanceMeters(step.distanceMeters())
                                                    .setDurationSeconds(step.durationSeconds())
                                                    .setGeometryStartIndex(step.geometryStartIndex())
                                                    .setGeometryEndIndex(step.geometryEndIndex())
                                                    .build())
                                            .toList());
                            if (leg.fromStopId() != null) {
                                builder.setFromStopId(leg.fromStopId().toString());
                            }
                            if (leg.toStopId() != null) {
                                builder.setToStopId(leg.toStopId().toString());
                            }
                            return builder.build();
                        })
                        .toList())
                .build();
    }

    private org.example.routingservice.grpc.routing.TravelMode toGrpcTravelMode(
            org.example.routingservice.dto.TravelMode travelMode
    ) {
        return switch (travelMode) {
            case DRIVE -> org.example.routingservice.grpc.routing.TravelMode.DRIVE;
            case WALK -> org.example.routingservice.grpc.routing.TravelMode.WALK;
            case BICYCLE -> org.example.routingservice.grpc.routing.TravelMode.BICYCLE;
        };
    }

    private Status mapStatus(ResponseStatusException ex) {
        return switch (ex.getStatusCode().value()) {
            case 400 -> Status.INVALID_ARGUMENT;
            case 422 -> Status.FAILED_PRECONDITION;
            case 429 -> Status.RESOURCE_EXHAUSTED;
            case 502, 503, 504 -> Status.UNAVAILABLE;
            default -> Status.INTERNAL;
        };
    }
}
