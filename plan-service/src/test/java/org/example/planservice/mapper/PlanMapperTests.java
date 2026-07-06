package org.example.planservice.mapper;

import org.example.planservice.dto.CreatePlanRequest;
import org.example.planservice.dto.PlanResponse;
import org.example.planservice.dto.PlanStopRequest;
import org.example.planservice.dto.PlanTimelineSegmentResponse;
import org.example.planservice.entity.Plan;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class PlanMapperTests {

    private final PlanMapper mapper = new PlanMapper();

    @Test
    void toResponseBuildsTimelineBetweenSortedStops() {
        CreatePlanRequest request = new CreatePlanRequest();
        request.setPlanName("Anniversary");
        request.setPlanDescription("Dinner and walk");
        request.setPlanStartDate(LocalDate.of(2026, 7, 6));
        request.setPlanEndDate(LocalDate.of(2026, 7, 6));
        request.setPlanStartTime(LocalTime.of(18, 0));
        request.setPlanEndTime(LocalTime.of(22, 0));
        request.setGroupId(UUID.randomUUID());
        request.setStops(List.of(
                stop("Dinner", 2, LocalTime.of(19, 0), LocalTime.of(20, 30)),
                stop("Cafe", 1, LocalTime.of(18, 0), LocalTime.of(18, 45)),
                stop("Walk", 3, LocalTime.of(20, 45), LocalTime.of(21, 30))
        ));

        Plan plan = mapper.toEntity(request, UUID.randomUUID());

        PlanResponse response = mapper.toResponse(plan);

        assertThat(response.getStops())
                .extracting("name")
                .containsExactly("Cafe", "Dinner", "Walk");
        assertThat(response.getStops().getFirst().getArrivalTime()).isEqualTo(LocalTime.of(18, 0));
        assertThat(response.getStops().getFirst().getDepartureTime()).isEqualTo(LocalTime.of(18, 45));
        assertThat(response.getTimeline()).hasSize(2);

        PlanTimelineSegmentResponse firstSegment = response.getTimeline().getFirst();
        assertThat(firstSegment.getFromStopName()).isEqualTo("Cafe");
        assertThat(firstSegment.getToStopName()).isEqualTo("Dinner");
        assertThat(firstSegment.getFromOrderIndex()).isEqualTo(1);
        assertThat(firstSegment.getToOrderIndex()).isEqualTo(2);
        assertThat(firstSegment.getFromDepartureTime()).isEqualTo(LocalTime.of(18, 45));
        assertThat(firstSegment.getToArrivalTime()).isEqualTo(LocalTime.of(19, 0));
    }

    private PlanStopRequest stop(String name, Integer orderIndex, LocalTime arrivalTime, LocalTime departureTime) {
        PlanStopRequest request = new PlanStopRequest();
        request.setName(name);
        request.setOrderIndex(orderIndex);
        request.setArrivalTime(arrivalTime);
        request.setDepartureTime(departureTime);
        return request;
    }
}
