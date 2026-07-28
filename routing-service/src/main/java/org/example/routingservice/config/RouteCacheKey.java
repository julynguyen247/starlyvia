package org.example.routingservice.config;

import org.example.routingservice.dto.ComputeRouteRequest;
import org.example.routingservice.dto.RouteStopRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component("routeCacheKey")
public class RouteCacheKey {
    public String from(ComputeRouteRequest request) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            update(digest, "route-v2");
            update(digest, request.travelMode().name());
            for (RouteStopRequest stop : request.stops()) {
                update(digest, stop.stopId() == null ? "" : stop.stopId().toString());
                update(digest, Double.toHexString(stop.latitude()));
                update(digest, Double.toHexString(stop.longitude()));
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private void update(MessageDigest digest, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        digest.update((byte) (bytes.length >>> 24));
        digest.update((byte) (bytes.length >>> 16));
        digest.update((byte) (bytes.length >>> 8));
        digest.update((byte) bytes.length);
        digest.update(bytes);
    }
}
