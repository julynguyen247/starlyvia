package org.example.planservice.mapper;

import org.example.planservice.dto.PlanStopRequest;
import org.example.planservice.dto.PlanStopResponse;
import org.example.planservice.dto.UpdatePlanStopRequest;
import org.example.planservice.entity.DatePlan;
import org.example.planservice.entity.PlanStop;
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
                .arrivalTime(request.getArrivalTime())
                .departureTime(request.getDepartureTime())
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
        planStop.setArrivalTime(request.getArrivalTime());
        planStop.setDepartureTime(request.getDepartureTime());
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
                planStop.getArrivalTime(),
                planStop.getDepartureTime(),
                planStop.getNote()
        );
    }
}
