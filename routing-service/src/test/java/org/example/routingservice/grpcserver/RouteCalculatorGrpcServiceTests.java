package org.example.routingservice.grpcserver;

import io.grpc.stub.StreamObserver;
import org.example.routingservice.dto.RouteCoordinateResponse;
import org.example.routingservice.dto.RouteLegResponse;
import org.example.routingservice.dto.RouteStepResponse;
import org.example.routingservice.dto.TravelMode;
import org.example.routingservice.grpc.routing.ComputeRouteRequest;
import org.example.routingservice.grpc.routing.ComputeRouteResponse;
import org.example.routingservice.grpc.routing.RouteStop;
import org.example.routingservice.service.RoutingService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouteCalculatorGrpcServiceTests {
    @Test
    void mapsGrpcRequestAndResponse() {
        RoutingService routingService = mock(RoutingService.class);
        RouteCalculatorGrpcService grpcService = new RouteCalculatorGrpcService(routingService);
        UUID firstStopId = UUID.randomUUID();
        UUID secondStopId = UUID.randomUUID();
        org.example.routingservice.dto.ComputeRouteResponse internalResponse =
                new org.example.routingservice.dto.ComputeRouteResponse(
                        "OPENROUTESERVICE",
                        TravelMode.DRIVE,
                        1521,
                        420,
                        List.of(
                                new RouteCoordinateResponse(10.77, 106.69),
                                new RouteCoordinateResponse(10.78, 106.70)
                        ),
                        List.of(new RouteLegResponse(
                                0,
                                1,
                                firstStopId,
                                secondStopId,
                                1521,
                                420,
                                List.of(new RouteStepResponse(
                                        11,
                                        "Head northeast on Test Street",
                                        "Test Street",
                                        1521,
                                        420,
                                        0,
                                        1
                                ))
                        ))
                );
        when(routingService.computeRoute(org.mockito.ArgumentMatchers.any()))
                .thenReturn(internalResponse);

        ComputeRouteRequest request = ComputeRouteRequest.newBuilder()
                .setTravelMode(org.example.routingservice.grpc.routing.TravelMode.DRIVE)
                .addStops(RouteStop.newBuilder()
                        .setStopId(firstStopId.toString())
                        .setLatitude(10.77)
                        .setLongitude(106.69)
                        .build())
                .addStops(RouteStop.newBuilder()
                        .setStopId(secondStopId.toString())
                        .setLatitude(10.78)
                        .setLongitude(106.70)
                        .build())
                .build();
        AtomicReference<ComputeRouteResponse> response = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        AtomicBoolean completed = new AtomicBoolean();

        grpcService.computeRoute(request, observer(response, error, completed));

        assertThat(error.get()).isNull();
        assertThat(completed).isTrue();
        assertThat(response.get().getProvider()).isEqualTo("OPENROUTESERVICE");
        assertThat(response.get().getDistanceMeters()).isEqualTo(1521);
        assertThat(response.get().getGeometryList()).hasSize(2);
        assertThat(response.get().getLegs(0).getFromStopId()).isEqualTo(firstStopId.toString());
        assertThat(response.get().getLegs(0).getSteps(0).getInstruction())
                .isEqualTo("Head northeast on Test Street");

        ArgumentCaptor<org.example.routingservice.dto.ComputeRouteRequest> captor =
                ArgumentCaptor.forClass(org.example.routingservice.dto.ComputeRouteRequest.class);
        verify(routingService).computeRoute(captor.capture());
        assertThat(captor.getValue().travelMode()).isEqualTo(TravelMode.DRIVE);
        assertThat(captor.getValue().stops())
                .extracting(stop -> stop.stopId())
                .containsExactly(firstStopId, secondStopId);
    }

    private StreamObserver<ComputeRouteResponse> observer(
            AtomicReference<ComputeRouteResponse> response,
            AtomicReference<Throwable> error,
            AtomicBoolean completed
    ) {
        return new StreamObserver<>() {
            @Override
            public void onNext(ComputeRouteResponse value) {
                response.set(value);
            }

            @Override
            public void onError(Throwable throwable) {
                error.set(throwable);
            }

            @Override
            public void onCompleted() {
                completed.set(true);
            }
        };
    }
}
