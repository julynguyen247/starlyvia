package org.example.placeservice.provider;

import org.example.placeservice.config.PlaceProviderProperties;
import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.dto.PlaceSuggestionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class GooglePlacesClientTests {
    @Test
    void mapsAutocompletePredictionsAndAppliesLimit() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GooglePlacesClient client = new GooglePlacesClient(builder.build(), properties("test-key"));

        server.expect(requestTo("https://places.test/v1/places:autocomplete"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Goog-Api-Key", "test-key"))
                .andExpect(content().json("""
                        {
                          "input": "museum",
                          "includeQueryPredictions": false,
                          "sessionToken": "session-1",
                          "locationBias": {
                            "circle": {
                              "center": {
                                "latitude": 10.77,
                                "longitude": 106.69
                              },
                              "radius": 5000
                            }
                          }
                        }
                        """))
                .andRespond(withSuccess("""
                        {
                          "suggestions": [
                            {
                              "placePrediction": {
                                "placeId": "place-1",
                                "text": {"text": "Museum One, Ho Chi Minh City"},
                                "structuredFormat": {
                                  "mainText": {"text": "Museum One"},
                                  "secondaryText": {"text": "Ho Chi Minh City"}
                                }
                              }
                            },
                            {
                              "placePrediction": {
                                "placeId": "place-2",
                                "text": {"text": "Museum Two"},
                                "structuredFormat": {
                                  "mainText": {"text": "Museum Two"}
                                }
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<PlaceSuggestionResponse> result = client.autocomplete(
                "museum",
                10.77,
                106.69,
                1,
                "session-1"
        );

        assertThat(result).singleElement().satisfies(place -> {
            assertThat(place.provider()).isEqualTo(PlaceProvider.GOOGLE);
            assertThat(place.providerPlaceId()).isEqualTo("place-1");
            assertThat(place.name()).isEqualTo("Museum One");
            assertThat(place.address()).isEqualTo("Ho Chi Minh City");
            assertThat(place.fullText()).isEqualTo("Museum One, Ho Chi Minh City");
        });
        server.verify();
    }

    @Test
    void mapsPlaceDetailsAndNormalizesResourceName() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GooglePlacesClient client = new GooglePlacesClient(builder.build(), properties("test-key"));

        server.expect(requestTo("https://places.test/v1/places/place-1"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Goog-Api-Key", "test-key"))
                .andRespond(withSuccess("""
                        {
                          "id": "place-1",
                          "displayName": {"text": "Museum One"},
                          "formattedAddress": "1 Main Street",
                          "location": {"latitude": 10.77, "longitude": 106.69},
                          "rating": 4.7,
                          "userRatingCount": 120,
                          "websiteUri": "https://museum.test",
                          "nationalPhoneNumber": "0123456789"
                        }
                        """, MediaType.APPLICATION_JSON));

        PlaceDetailsResponse result = client.details("places/place-1");

        assertThat(result.provider()).isEqualTo(PlaceProvider.GOOGLE);
        assertThat(result.providerPlaceId()).isEqualTo("place-1");
        assertThat(result.name()).isEqualTo("Museum One");
        assertThat(result.latitude()).isEqualTo(10.77);
        assertThat(result.longitude()).isEqualTo(106.69);
        assertThat(result.rating()).isEqualTo(4.7);
        assertThat(result.ratingCount()).isEqualTo(120);
        server.verify();
    }

    @Test
    void rejectsRequestsWhenApiKeyIsMissing() {
        GooglePlacesClient client = new GooglePlacesClient(RestClient.create(), properties(""));

        assertThatThrownBy(() -> client.autocomplete("museum", null, null, null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("API key is not configured");
    }

    private PlaceProviderProperties properties(String apiKey) {
        PlaceProviderProperties properties = new PlaceProviderProperties();
        properties.getGoogle().setApiKey(apiKey);
        properties.getGoogle().setBaseUrl("https://places.test/v1");
        return properties;
    }
}
