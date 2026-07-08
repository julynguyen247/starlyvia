package org.example.planservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
public class UpdatePlanStopRequest {
    @NotBlank
    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private String provider;

    private String providerPlaceId;

    private String photoUrl;

    private Double rating;

    private Integer ratingCount;

    private String websiteUrl;

    private String phoneNumber;

    private Integer orderIndex;

    @NotNull
    private LocalTime arrivalTime;

    @NotNull
    private LocalTime departureTime;

    private String note;
}
