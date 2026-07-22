package org.example.placeservice.service;

import org.example.placeservice.dto.PlaceDetailsResponse;
import org.example.placeservice.dto.PlaceProvider;
import org.example.placeservice.provider.GeoapifyPlacesClient;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PlaceServiceTests {
    private final GeoapifyPlacesClient geoapifyPlacesClient = mock(GeoapifyPlacesClient.class);
    private final PlaceService placeService = new PlaceService(geoapifyPlacesClient);

    @Test
    void rejectsLatitudeWithoutLongitude() {
        assertThatThrownBy(() -> placeService.autocomplete("museum", 10.77, null, 5, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("must be supplied together");

        verifyNoInteractions(geoapifyPlacesClient);
    }

    @Test
    void rejectsLongitudeWithoutLatitude() {
        assertThatThrownBy(() -> placeService.autocomplete("museum", null, 106.69, 5, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("must be supplied together");

        verifyNoInteractions(geoapifyPlacesClient);
    }

    @Test
    void routesGeoapifyDetailsToTheConfiguredProvider() {
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
                null,
                null
        );
        when(geoapifyPlacesClient.details("place-1")).thenReturn(details);

        PlaceDetailsResponse result = placeService.details(PlaceProvider.GEOAPIFY, "place-1");

        assertThat(result).isSameAs(details);
        verify(geoapifyPlacesClient).details("place-1");
    }

    @Test
    void rejectsAnInactiveLegacyProvider() {
        assertThatThrownBy(() -> placeService.details(PlaceProvider.GOOGLE, "place-1"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unsupported place provider");

        verifyNoInteractions(geoapifyPlacesClient);
    }
}
