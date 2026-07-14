package org.example.planservice.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.example.planservice.dto.ComputeRouteResponse;
import org.example.planservice.entity.PlanStop;
import org.example.planservice.grpc.routing.ComputeRouteRequest;
import org.example.planservice.grpc.routing.RouteCalculatorServiceGrpc;
import org.example.planservice.grpc.routing.RouteCoordinate;
import org.example.planservice.grpc.routing.RouteLeg;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class GrpcRoutingClientTests {
    private final AtomicReference<ComputeRouteRequest> receivedRequest = new AtomicReference<>();
    private Server server;
    private ManagedChannel channel;
    private GrpcRoutingClient client;

    @BeforeEach
    void setUp() throws IOException {
        server = ServerBuilder.forPort(0)
                .addService(new RouteCalculatorServiceGrpc.RouteCalculatorServiceImplBase() {
                    @Override
                    public void computeRoute(
                            ComputeRouteRequest request,
                            StreamObserver<org.example.planservice.grpc.routing.ComputeRouteResponse> observer
                    ) {
                        receivedRequest.set(request);
                        org.example.planservice.grpc.routing.ComputeRouteResponse response =
                                org.example.planservice.grpc.routing.ComputeRouteResponse.newBuilder()
                                        .setProvider("OPENROUTESERVICE")
                                        .setTravelMode(org.example.planservice.grpc.routing.TravelMode.DRIVE)
                                        .setDistanceMeters(1521)
                                        .setDurationSeconds(420)
                                        .addGeometry(RouteCoordinate.newBuilder()
                                                .setLatitude(10.77)
                                                .setLongitude(106.69)
                                                .build())
                                        .addLegs(RouteLeg.newBuilder()
                                                .setFromStopIndex(0)
                                                .setToStopIndex(1)
                                                .setFromStopId(request.getStops(0).getStopId())
                                                .setToStopId(request.getStops(1).getStopId())
                                                .setDistanceMeters(1521)
                                                .setDurationSeconds(420)
                                                .build())
                                        .build();
                        observer.onNext(response);
                        observer.onCompleted();
                    }
                })
                .build()
                .start();
        channel = ManagedChannelBuilder.forAddress("localhost", server.getPort())
                .usePlaintext()
                .build();
        client = new GrpcRoutingClient(RouteCalculatorServiceGrpc.newBlockingStub(channel));
    }

    @AfterEach
    void tearDown() throws InterruptedException {
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        server.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Test
    void sendsStopsAndMapsRouteResponse() {
        UUID firstStopId = UUID.randomUUID();
        UUID secondStopId = UUID.randomUUID();
        List<PlanStop> stops = List.of(
                stop(firstStopId, 10.77, 106.69),
                stop(secondStopId, 10.78, 106.70)
        );

        ComputeRouteResponse response = client.computeRoute(TravelMode.DRIVE, stops);

        assertThat(receivedRequest.get().getTravelMode())
                .isEqualTo(org.example.planservice.grpc.routing.TravelMode.DRIVE);
        assertThat(receivedRequest.get().getStopsList())
                .extracting(RouteStop -> RouteStop.getStopId())
                .containsExactly(firstStopId.toString(), secondStopId.toString());
        assertThat(response.provider()).isEqualTo("OPENROUTESERVICE");
        assertThat(response.distanceMeters()).isEqualTo(1521);
        assertThat(response.geometry()).hasSize(1);
        assertThat(response.legs()).singleElement().satisfies(leg -> {
            assertThat(leg.fromStopId()).isEqualTo(firstStopId);
            assertThat(leg.toStopId()).isEqualTo(secondStopId);
        });
    }

    private PlanStop stop(UUID id, double latitude, double longitude) {
        return PlanStop.builder()
                .id(id)
                .latitude(latitude)
                .longitude(longitude)
                .build();
    }
}
