package org.example.routingservice.provider;

import org.example.routingservice.config.OpenRouteServiceProperties;
import org.example.routingservice.dto.ComputeRouteRequest;
import org.example.routingservice.dto.ComputeRouteResponse;
import org.example.routingservice.dto.RouteStopRequest;
import org.example.routingservice.dto.TravelMode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenRouteServiceClientTests {
    @Test
    void mapsGeoJsonRouteToApplicationResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        OpenRouteServiceClient client = new OpenRouteServiceClient(
                builder.build(),
                new OpenRouteServiceProperties("test-key", "https://routing.test")
        );
        UUID firstStopId = UUID.randomUUID();
        UUID secondStopId = UUID.randomUUID();
        ComputeRouteRequest request = new ComputeRouteRequest(
                TravelMode.DRIVE,
                List.of(
                        new RouteStopRequest(firstStopId, 10.7700, 106.6900),
                        new RouteStopRequest(secondStopId, 10.7800, 106.7000)
                )
        );

        server.expect(requestTo("https://routing.test/v2/directions/driving-car/geojson"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("Authorization", "test-key"))
                .andExpect(content().json("""
                        {
                          "coordinates": [
                            [106.69, 10.77],
                            [106.7, 10.78]
                          ],
                          "instructions": false
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "type": "FeatureCollection",
                          "features": [{
                            "type": "Feature",
                            "properties": {
                              "summary": {
                                "distance": 1520.6,
                                "duration": 420.4
                              },
                              "segments": [{
                                "distance": 1520.6,
                                "duration": 420.4
                              }]
                            },
                            "geometry": {
                              "type": "LineString",
                              "coordinates": [
                                [106.69, 10.77],
                                [106.695, 10.775],
                                [106.7, 10.78]
                              ]
                            }
                          }]
                        }
                        """, MediaType.APPLICATION_JSON));

        ComputeRouteResponse response = client.computeRoute(request);

        assertThat(response.provider()).isEqualTo("OPENROUTESERVICE");
        assertThat(response.travelMode()).isEqualTo(TravelMode.DRIVE);
        assertThat(response.distanceMeters()).isEqualTo(1521);
        assertThat(response.durationSeconds()).isEqualTo(420);
        assertThat(response.geometry()).hasSize(3);
        assertThat(response.geometry().getFirst().latitude()).isEqualTo(10.77);
        assertThat(response.geometry().getFirst().longitude()).isEqualTo(106.69);
        assertThat(response.legs()).singleElement().satisfies(leg -> {
            assertThat(leg.fromStopId()).isEqualTo(firstStopId);
            assertThat(leg.toStopId()).isEqualTo(secondStopId);
            assertThat(leg.distanceMeters()).isEqualTo(1521);
        });
        server.verify();
    }

    @Test
    void rejectsRequestsWhenApiKeyIsMissing() {
        OpenRouteServiceClient client = new OpenRouteServiceClient(
                RestClient.create(),
                new OpenRouteServiceProperties("", "https://routing.test")
        );
        ComputeRouteRequest request = new ComputeRouteRequest(
                TravelMode.WALK,
                List.of(
                        new RouteStopRequest(null, 10.77, 106.69),
                        new RouteStopRequest(null, 10.78, 106.70)
                )
        );

        assertThatThrownBy(() -> client.computeRoute(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("API key is not configured");
    }
}
