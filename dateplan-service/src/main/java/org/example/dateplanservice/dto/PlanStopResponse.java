package org.example.dateplanservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlanStopResponse {
    private UUID id;
    private UUID datePlanId;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer orderIndex;
    private String note;
}
