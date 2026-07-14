package org.example.routingservice.dto;

public enum TravelMode {
    DRIVE("driving-car"),
    WALK("foot-walking"),
    BICYCLE("cycling-regular");

    private final String providerProfile;

    TravelMode(String providerProfile) {
        this.providerProfile = providerProfile;
    }

    public String providerProfile() {
        return providerProfile;
    }
}
