package org.example.planservice.service;

import lombok.RequiredArgsConstructor;
import org.example.planservice.dto.PlanStopRequest;
import org.example.planservice.dto.PlanStopResponse;
import org.example.planservice.dto.UpdatePlanStopRequest;
import org.example.planservice.entity.DatePlan;
import org.example.planservice.entity.PlanStop;
import org.example.planservice.mapper.PlanStopMapper;
import org.example.planservice.repository.DatePlanRepository;
import org.example.planservice.repository.PlanStopRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanStopService {
    private final PlanStopRepository planStopRepository;
    private final DatePlanRepository datePlanRepository;
    private final PlanStopMapper planStopMapper;
    private final DatePlanAccessPolicy datePlanAccessPolicy;

    @Transactional
    public PlanStopResponse create(UUID currentUserId, UUID datePlanId, PlanStopRequest request) {
        DatePlan datePlan = findDatePlan(datePlanId);
        datePlanAccessPolicy.assertCanEdit(datePlan, currentUserId);
        PlanStop planStop = planStopMapper.toEntity(request, datePlan);
        datePlan.setUpdatedAt(LocalDateTime.now());

        return planStopMapper.toResponse(planStopRepository.save(planStop));
    }

    @Transactional(readOnly = true)
    public PlanStopResponse getById(UUID currentUserId, UUID id) {
        PlanStop planStop = findPlanStop(id);
        datePlanAccessPolicy.assertCanView(planStop.getDatePlan(), currentUserId);
        return planStopMapper.toResponse(planStop);
    }

    @Transactional(readOnly = true)
    public List<PlanStopResponse> getByDatePlanId(UUID currentUserId, UUID datePlanId) {
        DatePlan datePlan = findDatePlan(datePlanId);
        datePlanAccessPolicy.assertCanView(datePlan, currentUserId);

        return planStopRepository.findByDatePlanId(datePlanId).stream()
                .sorted(Comparator.comparing(
                        PlanStop::getOrderIndex,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(planStopMapper::toResponse)
                .toList();
    }

    @Transactional
    public PlanStopResponse update(UUID currentUserId, UUID id, UpdatePlanStopRequest request) {
        PlanStop planStop = findPlanStop(id);
        datePlanAccessPolicy.assertCanEdit(planStop.getDatePlan(), currentUserId);
        planStopMapper.updateEntity(planStop, request);
        planStop.getDatePlan().setUpdatedAt(LocalDateTime.now());

        return planStopMapper.toResponse(planStopRepository.save(planStop));
    }

    @Transactional
    public void delete(UUID currentUserId, UUID id) {
        PlanStop planStop = findPlanStop(id);
        datePlanAccessPolicy.assertCanEdit(planStop.getDatePlan(), currentUserId);
        planStop.getDatePlan().setUpdatedAt(LocalDateTime.now());
        planStopRepository.delete(planStop);
    }

    private DatePlan findDatePlan(UUID id) {
        return datePlanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Date plan not found"));
    }

    private PlanStop findPlanStop(UUID id) {
        return planStopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan stop not found"));
    }
}
