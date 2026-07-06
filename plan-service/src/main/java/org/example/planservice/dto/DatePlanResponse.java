package org.example.planservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.planservice.enums.PlanStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class DatePlanResponse {
    private UUID id;
    private String planName;
    private String planDescription;
    private LocalDate planStartDate;
    private LocalDate planEndDate;
    private LocalTime planStartTime;
    private LocalTime planEndTime;
    private UUID groupId;
    private PlanStatus status;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PlanStopResponse> stops;
    private List<PlanTimelineSegmentResponse> timeline;
}
