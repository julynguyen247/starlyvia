package org.example.dateplanservice.service;

import lombok.RequiredArgsConstructor;
import org.example.dateplanservice.dto.CreateDatePlanRequest;
import org.example.dateplanservice.dto.DatePlanResponse;
import org.example.dateplanservice.dto.UpdateDatePlanRequest;
import org.example.dateplanservice.entity.DatePlan;
import org.example.dateplanservice.mapper.DatePlanMapper;
import org.example.dateplanservice.repository.DatePlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DatePlanService {
    private final DatePlanRepository datePlanRepository;
    private final DatePlanMapper datePlanMapper;

    @Transactional
    public DatePlanResponse create(UUID createdBy, CreateDatePlanRequest request) {
        DatePlan datePlan = datePlanMapper.toEntity(request, createdBy);
        return datePlanMapper.toResponse(datePlanRepository.save(datePlan));
    }

    @Transactional(readOnly = true)
    public DatePlanResponse getById(UUID id) {
        return datePlanMapper.toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<DatePlanResponse> getByCoupleId(UUID coupleId) {
        return datePlanRepository.findByCoupleId(coupleId).stream()
                .sorted(Comparator.comparing(
                        DatePlan::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(datePlanMapper::toResponse)
                .toList();
    }

    @Transactional
    public DatePlanResponse update(UUID id, UpdateDatePlanRequest request) {
        DatePlan datePlan = findById(id);
        datePlanMapper.updateEntity(datePlan, request);
        return datePlanMapper.toResponse(datePlanRepository.save(datePlan));
    }

    @Transactional
    public void delete(UUID id) {
        DatePlan datePlan = findById(id);
        datePlanRepository.delete(datePlan);
    }

    private DatePlan findById(UUID id) {
        return datePlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Date plan not found"));
    }
}
