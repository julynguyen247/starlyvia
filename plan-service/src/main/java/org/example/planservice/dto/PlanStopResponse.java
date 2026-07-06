package org.example.planservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlanStopResponse {
    private UUID id;
    private UUID planId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer orderIndex;
    private LocalTime arrivalTime;
    private LocalTime departureTime;
    private String note;
}
