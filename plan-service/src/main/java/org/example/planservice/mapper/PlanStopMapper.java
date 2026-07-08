package org.example.planservice.mapper;

import org.example.planservice.dto.PlanStopRequest;
import org.example.planservice.dto.PlanStopResponse;
import org.example.planservice.dto.UpdatePlanStopRequest;
import org.example.planservice.entity.Plan;
import org.example.planservice.entity.PlanStop;
import org.springframework.stereotype.Component;

@Component
public class PlanStopMapper {
    public PlanStop toEntity(PlanStopRequest request, Plan plan) {
        return PlanStop.builder()
                .name(request.getName())
                .address(request.getAddress())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .provider(request.getProvider())
                .providerPlaceId(request.getProviderPlaceId())
                .photoUrl(request.getPhotoUrl())
                .rating(request.getRating())
                .ratingCount(request.getRatingCount())
                .websiteUrl(request.getWebsiteUrl())
                .phoneNumber(request.getPhoneNumber())
                .orderIndex(request.getOrderIndex())
                .arrivalTime(request.getArrivalTime())
                .departureTime(request.getDepartureTime())
                .note(request.getNote())
                .plan(plan)
                .build();
    }

    public void updateEntity(PlanStop planStop, UpdatePlanStopRequest request) {
        planStop.setName(request.getName());
        planStop.setAddress(request.getAddress());
        planStop.setLatitude(request.getLatitude());
        planStop.setLongitude(request.getLongitude());
        planStop.setProvider(request.getProvider());
        planStop.setProviderPlaceId(request.getProviderPlaceId());
        planStop.setPhotoUrl(request.getPhotoUrl());
        planStop.setRating(request.getRating());
        planStop.setRatingCount(request.getRatingCount());
        planStop.setWebsiteUrl(request.getWebsiteUrl());
        planStop.setPhoneNumber(request.getPhoneNumber());
        planStop.setOrderIndex(request.getOrderIndex());
        planStop.setArrivalTime(request.getArrivalTime());
        planStop.setDepartureTime(request.getDepartureTime());
        planStop.setNote(request.getNote());
    }

    public PlanStopResponse toResponse(PlanStop planStop) {
        return new PlanStopResponse(
                planStop.getId(),
                planStop.getPlan().getId(),
                planStop.getName(),
                planStop.getAddress(),
                planStop.getLatitude(),
                planStop.getLongitude(),
                planStop.getProvider(),
                planStop.getProviderPlaceId(),
                planStop.getPhotoUrl(),
                planStop.getRating(),
                planStop.getRatingCount(),
                planStop.getWebsiteUrl(),
                planStop.getPhoneNumber(),
                planStop.getOrderIndex(),
                planStop.getArrivalTime(),
                planStop.getDepartureTime(),
                planStop.getNote()
        );
    }
}
