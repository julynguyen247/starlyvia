package org.example.routingservice.config;

import org.example.routingservice.dto.ComputeRouteResponse;
import org.example.routingservice.dto.RouteCoordinateResponse;
import org.example.routingservice.dto.RouteLegResponse;
import org.example.routingservice.dto.TravelMode;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RoutingRedisCacheSerializationTests {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void roundTripsComputedRouteValues() {
        UUID firstStopId = UUID.randomUUID();
        UUID secondStopId = UUID.randomUUID();
        ComputeRouteResponse response = new ComputeRouteResponse(
                "OPENROUTESERVICE",
                TravelMode.DRIVE,
                1521,
                420,
                List.of(
                        new RouteCoordinateResponse(10.77, 106.69),
                        new RouteCoordinateResponse(10.78, 106.70)
                ),
                List.of(new RouteLegResponse(0, 1, firstStopId, secondStopId, 1521, 420, List.of()))
        );
        JacksonJsonRedisSerializer<ComputeRouteResponse> serializer =
                new JacksonJsonRedisSerializer<>(objectMapper, ComputeRouteResponse.class);

        assertThat(serializer.deserialize(serializer.serialize(response))).isEqualTo(response);
    }
}
