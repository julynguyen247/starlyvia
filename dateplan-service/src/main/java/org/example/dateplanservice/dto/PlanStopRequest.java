package org.example.dateplanservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlanStopRequest {
    @NotBlank
    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private Integer orderIndex;

    private String note;
}
