package org.example.placeservice.dto;

public record PlaceSuggestionResponse(
        PlaceProvider provider,
        String providerPlaceId,
        String name,
        String address,
        String fullText
) {
}
