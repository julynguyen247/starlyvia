package org.example.planservice.mapper;

import org.example.planservice.dto.CreatePlanRequest;
import org.example.planservice.dto.PlanResponse;
import org.example.planservice.dto.PlanStopRequest;
import org.example.planservice.dto.PlanStopResponse;
import org.example.planservice.dto.PlanTimelineSegmentResponse;
import org.example.planservice.dto.UpplanRequest;
import org.example.planservice.entity.Plan;
import org.example.planservice.entity.PlanStop;
import org.example.planservice.enums.PlanStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class PlanMapper {
    public Plan toEntity(CreatePlanRequest request, UUID createdBy) {
        LocalDateTime now = LocalDateTime.now();
        Plan plan = Plan.builder()
                .planName(request.getPlanName())
                .planDescription(request.getPlanDescription())
                .planStartDate(request.getPlanStartDate())
                .planEndDate(request.getPlanEndDate())
                .planStartTime(request.getPlanStartTime())
                .planEndTime(request.getPlanEndTime())
                .groupId(request.getGroupId())
                .status(request.getStatus() == null ? PlanStatus.DRAFT : request.getStatus())
                .createdBy(createdBy)
                .createdAt(now)
                .updatedAt(now)
                .stops(new ArrayList<>())
                .build();

        addStops(plan, request.getStops());
        return plan;
    }

    public void updateEntity(Plan plan, UpplanRequest request) {
        plan.setPlanName(request.getPlanName());
        plan.setPlanDescription(request.getPlanDescription());
        plan.setPlanStartDate(request.getPlanStartDate());
        plan.setPlanEndDate(request.getPlanEndDate());
        plan.setPlanStartTime(request.getPlanStartTime());
        plan.setPlanEndTime(request.getPlanEndTime());
        plan.setStatus(request.getStatus());
        plan.setUpdatedAt(LocalDateTime.now());

        if (plan.getStops() == null) {
            plan.setStops(new ArrayList<>());
        } else {
            plan.getStops().clear();
        }
        addStops(plan, request.getStops());
    }

    public PlanResponse toResponse(Plan plan) {
        List<PlanStopResponse> stops = plan.getStops() == null
                ? List.of()
                : plan.getStops().stream()
                .sorted(Comparator.comparing(
                        PlanStop::getOrderIndex,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(this::toResponse)
                .toList();

        return new PlanResponse(
                plan.getId(),
                plan.getPlanName(),
                plan.getPlanDescription(),
                plan.getPlanStartDate(),
                plan.getPlanEndDate(),
                plan.getPlanStartTime(),
                plan.getPlanEndTime(),
                plan.getGroupId(),
                plan.getStatus(),
                plan.getCreatedBy(),
                plan.getCreatedAt(),
                plan.getUpdatedAt(),
                stops,
                buildTimeline(stops)
        );
    }

    private List<PlanTimelineSegmentResponse> buildTimeline(List<PlanStopResponse> stops) {
        if (stops.size() < 2) {
            return List.of();
        }

        List<PlanTimelineSegmentResponse> timeline = new ArrayList<>();
        for (int i = 0; i < stops.size() - 1; i++) {
            PlanStopResponse fromStop = stops.get(i);
            PlanStopResponse toStop = stops.get(i + 1);
            timeline.add(new PlanTimelineSegmentResponse(
                    fromStop.getId(),
                    toStop.getId(),
                    fromStop.getName(),
                    toStop.getName(),
                    fromStop.getOrderIndex(),
                    toStop.getOrderIndex(),
                    fromStop.getDepartureTime(),
                    toStop.getArrivalTime()
            ));
        }
        return timeline;
    }

    private void addStops(Plan plan, List<PlanStopRequest> stops) {
        if (stops == null) {
            return;
        }

        stops.stream()
                .map(stopRequest -> toEntity(stopRequest, plan))
                .forEach(plan.getStops()::add);
    }

    private PlanStop toEntity(PlanStopRequest request, Plan plan) {
        return PlanStop.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .orderIndex(request.getOrderIndex())
                .arrivalTime(request.getArrivalTime())
                .departureTime(request.getDepartureTime())
                .note(request.getNote())
                .plan(plan)
                .build();
    }

    private PlanStopResponse toResponse(PlanStop stop) {
        return new PlanStopResponse(
                stop.getId(),
                stop.getPlan().getId(),
                stop.getName(),
                stop.getAddress(),
                stop.getLatitude(),
                stop.getLongitude(),
                stop.getOrderIndex(),
                stop.getArrivalTime(),
                stop.getDepartureTime(),
                stop.getNote()
        );
    }
}
