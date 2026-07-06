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
import java.util.UUID;

@Getter
@Setter
public class CreateDatePlanRequest {
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

    private UUID groupId;

    private PlanStatus status;

    @Valid
    private List<PlanStopRequest> stops = new ArrayList<>();
}
