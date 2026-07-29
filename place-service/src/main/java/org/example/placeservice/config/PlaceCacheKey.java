package org.example.placeservice.config;

import org.example.placeservice.dto.PlaceProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component("placeCacheKey")
public class PlaceCacheKey {
    public String autocomplete(
            String query,
            Double latitude,
            Double longitude,
            Integer limit
    ) {
        return digest(
                "autocomplete-v1",
                exactText(query),
                normalizedNumber(latitude),
                normalizedNumber(longitude),
                normalizedInteger(limit)
        );
    }

    public String nearby(
            Double latitude,
            Double longitude,
            String type,
            Integer radiusMeters,
            Integer limit
    ) {
        return digest(
                "nearby-v2",
                normalizedNumber(latitude),
                normalizedNumber(longitude),
                exactText(type),
                normalizedInteger(radiusMeters),
                normalizedInteger(limit)
        );
    }

    public String viewport(
            Double west,
            Double south,
            Double east,
            Double north,
            String type,
            Integer limit
    ) {
        return digest(
                "viewport-v3",
                normalizedNumber(west),
                normalizedNumber(south),
                normalizedNumber(east),
                normalizedNumber(north),
                exactText(type),
                normalizedInteger(limit)
        );
    }

    public String details(PlaceProvider provider, String providerPlaceId) {
        return digest(
                "details-v1",
                provider == null ? "" : provider.name(),
                exactText(providerPlaceId)
        );
    }

    private String exactText(String value) {
        return value == null ? "" : value;
    }

    private String normalizedNumber(Double value) {
        if (value == null) {
            return "";
        }
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private String normalizedInteger(Integer value) {
        return value == null ? "" : value.toString();
    }

    private String digest(String... parts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String part : parts) {
                byte[] bytes = part.getBytes(StandardCharsets.UTF_8);
                digest.update((byte) (bytes.length >>> 24));
                digest.update((byte) (bytes.length >>> 16));
                digest.update((byte) (bytes.length >>> 8));
                digest.update((byte) bytes.length);
                digest.update(bytes);
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
