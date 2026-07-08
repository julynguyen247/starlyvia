package org.example.planservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.planservice.dto.PlanStopRequest;
import org.example.planservice.dto.PlanStopResponse;
import org.example.planservice.dto.UpdatePlanStopRequest;
import org.example.planservice.service.PlanStopService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PlanStopController {
    private final PlanStopService planStopService;

    @PostMapping("/api/v1/plans/{planId}/stops")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanStopResponse create(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID planId,
            @Valid @RequestBody PlanStopRequest request
    ) {
        return planStopService.create(currentUserId, planId, request);
    }

    @GetMapping("/api/v1/plans/{planId}/stops")
    public List<PlanStopResponse> getByPlanId(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID planId
    ) {
        return planStopService.getByPlanId(currentUserId, planId);
    }

    @GetMapping("/api/v1/plan-stops/{id}")
    public PlanStopResponse getById(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id
    ) {
        return planStopService.getById(currentUserId, id);
    }

    @PutMapping("/api/v1/plan-stops/{id}")
    public PlanStopResponse update(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanStopRequest request
    ) {
        return planStopService.update(currentUserId, id, request);
    }

    @DeleteMapping("/api/v1/plan-stops/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id
    ) {
        planStopService.delete(currentUserId, id);
    }
}
