package org.example.planservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.planservice.enums.PlanStatus;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class UpdatePlanRequest {
    @NotBlank
    private String planName;

    @NotBlank
    private String planDescription;

    @NotNull
    private LocalDate planStartDate;

    @NotNull
    private LocalDate planEndDate;

    @NotNull
    private LocalTime planStartTime;

    @NotNull
    private LocalTime planEndTime;

    @NotNull
    private PlanStatus status;

    @Valid
    private List<PlanStopRequest> stops = new ArrayList<>();
}
