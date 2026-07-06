package org.example.planservice.service;

import lombok.RequiredArgsConstructor;
import org.example.planservice.dto.PlanStopRequest;
import org.example.planservice.dto.PlanStopResponse;
import org.example.planservice.dto.UpplanStopRequest;
import org.example.planservice.entity.Plan;
import org.example.planservice.entity.PlanStop;
import org.example.planservice.mapper.PlanStopMapper;
import org.example.planservice.repository.PlanRepository;
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
    private final PlanRepository planRepository;
    private final PlanStopMapper planStopMapper;
    private final PlanAccessPolicy planAccessPolicy;

    @Transactional
    public PlanStopResponse create(UUID currentUserId, UUID planId, PlanStopRequest request) {
        Plan plan = findPlan(planId);
        planAccessPolicy.assertCanEdit(plan, currentUserId);
        PlanStop planStop = planStopMapper.toEntity(request, plan);
        plan.setUpdatedAt(LocalDateTime.now());

        return planStopMapper.toResponse(planStopRepository.save(planStop));
    }

    @Transactional(readOnly = true)
    public PlanStopResponse getById(UUID currentUserId, UUID id) {
        PlanStop planStop = findPlanStop(id);
        planAccessPolicy.assertCanView(planStop.getPlan(), currentUserId);
        return planStopMapper.toResponse(planStop);
    }

    @Transactional(readOnly = true)
    public List<PlanStopResponse> getByPlanId(UUID currentUserId, UUID planId) {
        Plan plan = findPlan(planId);
        planAccessPolicy.assertCanView(plan, currentUserId);

        return planStopRepository.findByPlanId(planId).stream()
                .sorted(Comparator.comparing(
                        PlanStop::getOrderIndex,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .map(planStopMapper::toResponse)
                .toList();
    }

    @Transactional
    public PlanStopResponse update(UUID currentUserId, UUID id, UpplanStopRequest request) {
        PlanStop planStop = findPlanStop(id);
        planAccessPolicy.assertCanEdit(planStop.getPlan(), currentUserId);
        planStopMapper.updateEntity(planStop, request);
        planStop.getPlan().setUpdatedAt(LocalDateTime.now());

        return planStopMapper.toResponse(planStopRepository.save(planStop));
    }

    @Transactional
    public void delete(UUID currentUserId, UUID id) {
        PlanStop planStop = findPlanStop(id);
        planAccessPolicy.assertCanEdit(planStop.getPlan(), currentUserId);
        planStop.getPlan().setUpdatedAt(LocalDateTime.now());
        planStopRepository.delete(planStop);
    }

    private Plan findPlan(UUID id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Date plan not found"));
    }

    private PlanStop findPlanStop(UUID id) {
        return planStopRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan stop not found"));
    }
}
