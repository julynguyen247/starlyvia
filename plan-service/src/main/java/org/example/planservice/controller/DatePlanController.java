package org.example.planservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.planservice.dto.CreateDatePlanRequest;
import org.example.planservice.dto.DatePlanResponse;
import org.example.planservice.dto.UpdateDatePlanRequest;
import org.example.planservice.service.DatePlanService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/date-plans")
@RequiredArgsConstructor
public class DatePlanController {
    private final DatePlanService datePlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DatePlanResponse create(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody CreateDatePlanRequest request
    ) {
        return datePlanService.create(currentUserId, request);
    }

    @GetMapping("/{id}")
    public DatePlanResponse getById(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id
    ) {
        return datePlanService.getById(currentUserId, id);
    }

    @GetMapping("/groups/{groupId}")
    public List<DatePlanResponse> getByGroupId(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID groupId
    ) {
        return datePlanService.getByGroupId(currentUserId, groupId);
    }

    @PutMapping("/{id}")
    public DatePlanResponse update(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDatePlanRequest request
    ) {
        return datePlanService.update(currentUserId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id
    ) {
        datePlanService.delete(currentUserId, id);
    }
}
