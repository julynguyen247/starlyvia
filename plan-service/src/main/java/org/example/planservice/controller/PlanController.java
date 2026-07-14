package org.example.planservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.planservice.client.TravelMode;
import org.example.planservice.dto.ComputeRouteResponse;
import org.example.planservice.dto.CreatePlanRequest;
import org.example.planservice.dto.PlanResponse;
import org.example.planservice.dto.UpdatePlanRequest;
import org.example.planservice.service.PlanRouteService;
import org.example.planservice.service.PlanService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {
    private final PlanService planService;
    private final PlanRouteService planRouteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse create(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody CreatePlanRequest request
    ) {
        return planService.create(currentUserId, request);
    }

    @GetMapping("/{id}")
    public PlanResponse getById(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id
    ) {
        return planService.getById(currentUserId, id);
    }

    @GetMapping("/{id}/route")
    public ComputeRouteResponse computeRoute(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "DRIVE") TravelMode travelMode
    ) {
        return planRouteService.computeRoute(currentUserId, id, travelMode);
    }

    @GetMapping("/groups/{groupId}")
    public List<PlanResponse> getByGroupId(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID groupId
    ) {
        return planService.getByGroupId(currentUserId, groupId);
    }

    @PutMapping("/{id}")
    public PlanResponse update(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePlanRequest request
    ) {
        return planService.update(currentUserId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id
    ) {
        planService.delete(currentUserId, id);
    }
}
