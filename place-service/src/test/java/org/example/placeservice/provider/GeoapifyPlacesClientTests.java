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
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withTooManyRequests;

class GeoapifyPlacesClientTests {
    @Test
    void mapsAutocompleteFeaturesAndAppliesLocationBias() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeoapifyPlacesClient client = new GeoapifyPlacesClient(builder.build(), properties("test-key"));

        server.expect(requestTo(startsWith("https://places.test/v1/geocode/autocomplete?")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("text", "museum"))
                .andExpect(queryParam("format", "geojson"))
                .andExpect(queryParam("limit", "1"))
                .andExpect(queryParam("bias", "proximity:106.69,10.77"))
                .andExpect(queryParam("apiKey", "test-key"))
                .andRespond(withSuccess("""
                        {
                          "type": "FeatureCollection",
                          "features": [
                            {
                              "type": "Feature",
                              "properties": {
                                "place_id": "place-1",
                                "name": "Museum One",
                                "address_line2": "Ho Chi Minh City, Vietnam",
                                "formatted": "Museum One, Ho Chi Minh City, Vietnam"
                              },
                              "geometry": {"type": "Point", "coordinates": [106.69, 10.77]}
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<PlaceSuggestionResponse> result = client.autocomplete(
                "museum",
                10.77,
                106.69,
                1,
                "ignored-session"
        );

        assertThat(result).singleElement().satisfies(place -> {
            assertThat(place.provider()).isEqualTo(PlaceProvider.GEOAPIFY);
            assertThat(place.providerPlaceId()).isEqualTo("place-1");
            assertThat(place.name()).isEqualTo("Museum One");
            assertThat(place.address()).isEqualTo("Ho Chi Minh City, Vietnam");
            assertThat(place.fullText()).isEqualTo("Museum One, Ho Chi Minh City, Vietnam");
        });
        server.verify();
    }

    @Test
    void mapsDetailsFeatureIncludingContactAndImage() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeoapifyPlacesClient client = new GeoapifyPlacesClient(builder.build(), properties("test-key"));

        server.expect(requestTo(startsWith("https://places.test/v2/place-details?")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("id", "place-1"))
                .andExpect(queryParam("features", "details"))
                .andExpect(queryParam("apiKey", "test-key"))
                .andRespond(withSuccess("""
                        {
                          "type": "FeatureCollection",
                          "features": [
                            {
                              "type": "Feature",
                              "properties": {
                                "feature_type": "details",
                                "name": "Museum One",
                                "formatted": "1 Main Street",
                                "lat": 10.77,
                                "lon": 106.69,
                                "website": "https://museum.test",
                                "contact": {"phone": "0123456789"},
                                "wiki_and_media": {"image": "https://images.test/museum.jpg"}
                              },
                              "geometry": {"type": "Polygon", "coordinates": []}
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        PlaceDetailsResponse result = client.details("place-1");

        assertThat(result.provider()).isEqualTo(PlaceProvider.GEOAPIFY);
        assertThat(result.providerPlaceId()).isEqualTo("place-1");
        assertThat(result.name()).isEqualTo("Museum One");
        assertThat(result.latitude()).isEqualTo(10.77);
        assertThat(result.longitude()).isEqualTo(106.69);
        assertThat(result.photoUrl()).isEqualTo("https://images.test/museum.jpg");
        assertThat(result.websiteUrl()).isEqualTo("https://museum.test");
        assertThat(result.phoneNumber()).isEqualTo("0123456789");
        assertThat(result.rating()).isNull();
        assertThat(result.ratingCount()).isNull();
        server.verify();
    }

    @Test
    void mapsNearbyPlacesWithConfiguredCategories() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeoapifyPlacesClient client = new GeoapifyPlacesClient(builder.build(), properties("test-key"));

        server.expect(requestTo(startsWith("https://places.test/v2/places?")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("categories", "accommodation,catering,commercial,education,healthcare,entertainment,leisure,tourism,service,religion,sport,public_transport,parking,rental,childcare"))
                .andExpect(queryParam("filter", "circle:106.69,10.77,1200"))
                .andExpect(queryParam("bias", "proximity:106.69,10.77"))
                .andExpect(queryParam("limit", "3"))
                .andRespond(withSuccess("""
                        {
                          "type": "FeatureCollection",
                          "features": [
                            {
                              "properties": {
                                "place_id": "cafe-1",
                                "name": "Play Cafe",
                                "formatted": "2 Lime Street",
                                "lat": 10.771,
                                "lon": 106.691,
                                "categories": ["catering.cafe", "catering"]
                              },
                              "geometry": {"type": "Point", "coordinates": [106.691, 10.771]}
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<PlaceDetailsResponse> result = client.nearby(10.77, 106.69, null, 1200, 3);

        assertThat(result).singleElement().satisfies(place -> {
            assertThat(place.provider()).isEqualTo(PlaceProvider.GEOAPIFY);
            assertThat(place.name()).isEqualTo("Play Cafe");
            assertThat(place.latitude()).isEqualTo(10.771);
            assertThat(place.longitude()).isEqualTo(106.691);
            assertThat(place.categories()).contains("catering.cafe");
        });
        server.verify();
    }

    @Test
    void mapsPlacesInsideAViewportWithAHighDensityLimit() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeoapifyPlacesClient client = new GeoapifyPlacesClient(builder.build(), properties("test-key"));

        server.expect(requestTo(startsWith("https://places.test/v2/places?")))
                .andExpect(method(HttpMethod.GET))
                .andExpect(queryParam("categories", "commercial"))
                .andExpect(queryParam("filter", "rect:106.68,10.79,106.72,10.75"))
                .andExpect(queryParam("bias", "proximity:106.7,10.77"))
                .andExpect(queryParam("limit", "100"))
                .andExpect(queryParam("apiKey", "test-key"))
                .andRespond(withSuccess("""
                        {
                          "type": "FeatureCollection",
                          "features": [
                            {
                              "properties": {
                                "place_id": "shop-1",
                                "name": "Corner Shop",
                                "formatted": "3 Market Street",
                                "lat": 10.771,
                                "lon": 106.701,
                                "categories": ["commercial.convenience", "commercial"]
                              }
                            }
                          ]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<PlaceDetailsResponse> result = client.viewport(
                106.68,
                10.75,
                106.72,
                10.79,
                "commercial",
                100
        );

        assertThat(result).singleElement().satisfies(place -> {
            assertThat(place.name()).isEqualTo("Corner Shop");
            assertThat(place.categories()).contains("commercial.convenience");
        });
        server.verify();
    }

    @Test
    void balancesDefaultViewportPlacesAcrossCategoryGroups() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeoapifyPlacesClient client = new GeoapifyPlacesClient(builder.build(), properties("test-key"));
        List<String> groups = List.of(
                "catering,commercial",
                "tourism,entertainment,leisure,sport,religion",
                "service,education,healthcare,public_transport,parking,rental,childcare",
                "accommodation"
        );

        for (int index = 0; index < groups.size(); index++) {
            int placeNumber = index + 1;
            server.expect(requestTo(startsWith("https://places.test/v2/places?")))
                    .andExpect(queryParam("categories", groups.get(index)))
                    .andExpect(queryParam("limit", "2"))
                    .andRespond(withSuccess("""
                            {
                              "features": [{
                                "properties": {
                                  "place_id": "place-%d",
                                  "name": "Place %d",
                                  "lat": 10.77,
                                  "lon": 106.69,
                                  "categories": ["%s"]
                                }
                              }]
                            }
                            """.formatted(placeNumber, placeNumber, groups.get(index).split(",")[0]), MediaType.APPLICATION_JSON));
        }

        List<PlaceDetailsResponse> result = client.viewport(106.68, 10.75, 106.72, 10.79, null, 8);

        assertThat(result).extracting(PlaceDetailsResponse::providerPlaceId)
                .containsExactly("place-1", "place-2", "place-3", "place-4");
        server.verify();
    }

    @Test
    void rejectsRequestsWhenApiKeyIsMissing() {
        GeoapifyPlacesClient client = new GeoapifyPlacesClient(RestClient.create(), properties(""));

        assertThatThrownBy(() -> client.autocomplete("museum", null, null, null, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Geoapify API key is not configured");
    }

    @Test
    void normalizesProviderFailuresWithoutExposingTheCredential() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        GeoapifyPlacesClient client = new GeoapifyPlacesClient(builder.build(), properties("secret-test-key"));

        server.expect(requestTo(startsWith("https://places.test/v1/geocode/autocomplete?")))
                .andRespond(withTooManyRequests());

        assertThatThrownBy(() -> client.autocomplete("museum", null, null, 3, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("502 BAD_GATEWAY")
                .hasMessageNotContaining("secret-test-key");
        server.verify();
    }

    private PlaceProviderProperties properties(String apiKey) {
        PlaceProviderProperties properties = new PlaceProviderProperties();
        properties.getGeoapify().setApiKey(apiKey);
        properties.getGeoapify().setBaseUrl("https://places.test");
        return properties;
    }
}
