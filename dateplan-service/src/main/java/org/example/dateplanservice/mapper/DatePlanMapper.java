package org.example.dateplanservice.mapper;

import org.example.dateplanservice.dto.CreateDatePlanRequest;
import org.example.dateplanservice.dto.DatePlanResponse;
import org.example.dateplanservice.dto.PlanStopRequest;
import org.example.dateplanservice.dto.PlanStopResponse;
import org.example.dateplanservice.dto.UpdateDatePlanRequest;
import org.example.dateplanservice.entity.DatePlan;
import org.example.dateplanservice.entity.PlanStop;
import org.example.dateplanservice.enums.PlanStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Component
public class DatePlanMapper {
    public DatePlan toEntity(CreateDatePlanRequest request, UUID createdBy) {
        LocalDateTime now = LocalDateTime.now();
        DatePlan datePlan = DatePlan.builder()
                .planName(request.getPlanName())
                .planDescription(request.getPlanDescription())
                .planStartDate(request.getPlanStartDate())
                .planEndDate(request.getPlanEndDate())
                .planStartTime(request.getPlanStartTime())
                .planEndTime(request.getPlanEndTime())
                .coupleId(request.getCoupleId())
                .status(request.getStatus() == null ? PlanStatus.DRAFT : request.getStatus())
                .createdBy(createdBy)
                .createdAt(now)
                .updatedAt(now)
                .stops(new ArrayList<>())
                .build();

        addStops(datePlan, request.getStops());
        return datePlan;
    }

    public void updateEntity(DatePlan datePlan, UpdateDatePlanRequest request) {
        datePlan.setPlanName(request.getPlanName());
        datePlan.setPlanDescription(request.getPlanDescription());
        datePlan.setPlanStartDate(request.getPlanStartDate());
        datePlan.setPlanEndDate(request.getPlanEndDate());
        datePlan.setPlanStartTime(request.getPlanStartTime());
        datePlan.setPlanEndTime(request.getPlanEndTime());
        datePlan.setStatus(request.getStatus());
        datePlan.setUpdatedAt(LocalDateTime.now());

        if (datePlan.getStops() == null) {
            datePlan.setStops(new ArrayList<>());
        } else {
            datePlan.getStops().clear();
        }
        addStops(datePlan, request.getStops());
    }

    public DatePlanResponse toResponse(DatePlan datePlan) {
        List<PlanStopResponse> stops = datePlan.getStops() == null
                ? List.of()
                : datePlan.getStops().stream()
                .sorted(Comparator.comparing(
                        PlanStop::getOrderIndex,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(this::toResponse)
                .toList();

        return new DatePlanResponse(
                datePlan.getId(),
                datePlan.getPlanName(),
                datePlan.getPlanDescription(),
                datePlan.getPlanStartDate(),
                datePlan.getPlanEndDate(),
                datePlan.getPlanStartTime(),
                datePlan.getPlanEndTime(),
                datePlan.getCoupleId(),
                datePlan.getStatus(),
                datePlan.getCreatedBy(),
                datePlan.getCreatedAt(),
                datePlan.getUpdatedAt(),
                stops
        );
    }

    private void addStops(DatePlan datePlan, List<PlanStopRequest> stops) {
        if (stops == null) {
            return;
        }

        stops.stream()
                .map(stopRequest -> toEntity(stopRequest, datePlan))
                .forEach(datePlan.getStops()::add);
    }

    private PlanStop toEntity(PlanStopRequest request, DatePlan datePlan) {
        return PlanStop.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .orderIndex(request.getOrderIndex())
                .note(request.getNote())
                .datePlan(datePlan)
                .build();
    }

    private PlanStopResponse toResponse(PlanStop stop) {
        return new PlanStopResponse(
                stop.getId(),
                stop.getDatePlan().getId(),
                stop.getName(),
                stop.getAddress(),
                stop.getLatitude(),
                stop.getLongitude(),
                stop.getOrderIndex(),
                stop.getNote()
        );
    }
}
