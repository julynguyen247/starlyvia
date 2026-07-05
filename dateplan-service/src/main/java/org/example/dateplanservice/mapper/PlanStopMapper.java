package org.example.dateplanservice.mapper;

import org.example.dateplanservice.dto.PlanStopRequest;
import org.example.dateplanservice.dto.PlanStopResponse;
import org.example.dateplanservice.dto.UpdatePlanStopRequest;
import org.example.dateplanservice.entity.DatePlan;
import org.example.dateplanservice.entity.PlanStop;
import org.springframework.stereotype.Component;

@Component
public class PlanStopMapper {
    public PlanStop toEntity(PlanStopRequest request, DatePlan datePlan) {
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

    public void updateEntity(PlanStop planStop, UpdatePlanStopRequest request) {
        planStop.setName(request.getName());
        planStop.setAddress(request.getAddress());
        planStop.setLatitude(request.getLatitude());
        planStop.setLongitude(request.getLongitude());
        planStop.setOrderIndex(request.getOrderIndex());
        planStop.setNote(request.getNote());
    }

    public PlanStopResponse toResponse(PlanStop planStop) {
        return new PlanStopResponse(
                planStop.getId(),
                planStop.getDatePlan().getId(),
                planStop.getName(),
                planStop.getAddress(),
                planStop.getLatitude(),
                planStop.getLongitude(),
                planStop.getOrderIndex(),
                planStop.getNote()
        );
    }
}
