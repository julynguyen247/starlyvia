package org.example.placeservice.service;

import org.example.placeservice.provider.GooglePlacesClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class PlaceServiceTests {
    private final GooglePlacesClient googlePlacesClient = mock(GooglePlacesClient.class);
    private final PlaceService placeService = new PlaceService(googlePlacesClient);

    @Test
    void rejectsLatitudeWithoutLongitude() {
        assertThatThrownBy(() -> placeService.autocomplete("museum", 10.77, null, 5, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("must be supplied together");

        verifyNoInteractions(googlePlacesClient);
    }

    @Test
    void rejectsLongitudeWithoutLatitude() {
        assertThatThrownBy(() -> placeService.autocomplete("museum", null, 106.69, 5, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("must be supplied together");

        verifyNoInteractions(googlePlacesClient);
    }
}
