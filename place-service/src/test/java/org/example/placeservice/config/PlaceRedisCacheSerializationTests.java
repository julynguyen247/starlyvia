package org.example.placeservice.config;

import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.dto.PlaceSuggestionResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlaceRedisCacheSerializationTests {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void roundTripsAutocompleteAndPlaceDetailsValues() {
        List<PlaceSuggestionResponse> suggestions = List.of(new PlaceSuggestionResponse(
                PlaceProvider.GEOAPIFY,
                "place-1",
                "Museum One",
                "Ho Chi Minh City",
                "Museum One, Ho Chi Minh City"
        ));
        PlaceDetailsResponse details = new PlaceDetailsResponse(
                PlaceProvider.GEOAPIFY,
                "place-1",
                "Museum One",
                "1 Main Street",
                10.77,
                106.69,
                null,
                null,
                null,
                "https://museum.test",
                "0123456789",
                List.of("entertainment.museum")
        );

        JavaType suggestionListType = objectMapper.getTypeFactory()
                .constructCollectionType(List.class, PlaceSuggestionResponse.class);
        JacksonJsonRedisSerializer<List<PlaceSuggestionResponse>> suggestionSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, suggestionListType);
        JacksonJsonRedisSerializer<PlaceDetailsResponse> detailsSerializer =
                new JacksonJsonRedisSerializer<>(objectMapper, PlaceDetailsResponse.class);

        assertThat(suggestionSerializer.deserialize(suggestionSerializer.serialize(suggestions)))
                .isEqualTo(suggestions);
        assertThat(detailsSerializer.deserialize(detailsSerializer.serialize(details)))
                .isEqualTo(details);
    }
}
