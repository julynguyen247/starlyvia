package org.example.placeservice.dto;

import java.util.List;

public record PlaceDetailsResponse(
        PlaceProvider provider,
        String providerPlaceId,
        String name,
        String address,
        Double latitude,
        Double longitude,
        String photoUrl,
        Double rating,
        Integer ratingCount,
        String websiteUrl,
        String phoneNumber,
        List<String> categories
) {
}
