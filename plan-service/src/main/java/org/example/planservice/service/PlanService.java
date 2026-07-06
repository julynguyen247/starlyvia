package org.example.planservice.service;

import lombok.RequiredArgsConstructor;
import org.example.planservice.client.GroupClient;
import org.example.planservice.dto.CreatePlanRequest;
import org.example.planservice.dto.PlanResponse;
import org.example.planservice.dto.UpplanRequest;
import org.example.planservice.entity.Plan;
import org.example.planservice.mapper.PlanMapper;
import org.example.planservice.repository.PlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final PlanMapper planMapper;
    private final GroupClient groupClient;
    private final PlanAccessPolicy planAccessPolicy;

    @Transactional
    public PlanResponse create(UUID createdBy, CreatePlanRequest request) {
        Plan plan = planMapper.toEntity(request, createdBy);
        plan.setGroupId(resolveGroupId(createdBy, request.getGroupId()));
        return planMapper.toResponse(planRepository.save(plan));
    }

    @Transactional(readOnly = true)
    public PlanResponse getById(UUID currentUserId, UUID id) {
        Plan plan = findById(id);
        planAccessPolicy.assertCanView(plan, currentUserId);
        return planMapper.toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> getByGroupId(UUID currentUserId, UUID groupId) {
        if (!groupClient.isUserInGroup(currentUserId, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not part of this group");
        }
        return planRepository.findByGroupId(groupId).stream()
                .sorted(Comparator.comparing(
                        Plan::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(planMapper::toResponse)
                .toList();
    }

    @Transactional
    public PlanResponse update(UUID currentUserId, UUID id, UpplanRequest request) {
        Plan plan = findById(id);
        planAccessPolicy.assertCanEdit(plan, currentUserId);
        planMapper.updateEntity(plan, request);
        return planMapper.toResponse(planRepository.save(plan));
    }

    @Transactional
    public void delete(UUID currentUserId, UUID id) {
        Plan plan = findById(id);
        planAccessPolicy.assertCanEdit(plan, currentUserId);
        planRepository.delete(plan);
    }

    private Plan findById(UUID id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Date plan not found"));
    }

    private UUID resolveGroupId(UUID createdBy, UUID requestedGroupId) {
        if (requestedGroupId == null) {
            return null;
        }

        if (!groupClient.isUserInGroup(createdBy, requestedGroupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not part of this group");
        }
        return requestedGroupId;
    }
}
