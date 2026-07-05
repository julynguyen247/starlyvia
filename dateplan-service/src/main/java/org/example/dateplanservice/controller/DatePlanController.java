package org.example.dateplanservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dateplanservice.dto.CreateDatePlanRequest;
import org.example.dateplanservice.dto.DatePlanResponse;
import org.example.dateplanservice.dto.UpdateDatePlanRequest;
import org.example.dateplanservice.service.DatePlanService;
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
    public DatePlanResponse getById(@PathVariable UUID id) {
        return datePlanService.getById(id);
    }

    @GetMapping("/couples/{coupleId}")
    public List<DatePlanResponse> getByCoupleId(@PathVariable UUID coupleId) {
        return datePlanService.getByCoupleId(coupleId);
    }

    @PutMapping("/{id}")
    public DatePlanResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDatePlanRequest request
    ) {
        return datePlanService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        datePlanService.delete(id);
    }
}
