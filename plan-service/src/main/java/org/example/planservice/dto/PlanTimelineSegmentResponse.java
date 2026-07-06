package org.example.planservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class PlanTimelineSegmentResponse {
    private UUID fromStopId;
    private UUID toStopId;
    private String fromStopName;
    private String toStopName;
    private Integer fromOrderIndex;
    private Integer toOrderIndex;
    private LocalTime fromDepartureTime;
    private LocalTime toArrivalTime;
}
