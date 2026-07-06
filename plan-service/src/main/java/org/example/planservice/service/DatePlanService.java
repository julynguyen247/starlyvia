package org.example.planservice.service;

import lombok.RequiredArgsConstructor;
import org.example.planservice.client.GroupClient;
import org.example.planservice.dto.CreateDatePlanRequest;
import org.example.planservice.dto.DatePlanResponse;
import org.example.planservice.dto.UpdateDatePlanRequest;
import org.example.planservice.entity.DatePlan;
import org.example.planservice.mapper.DatePlanMapper;
import org.example.planservice.repository.DatePlanRepository;
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
    private final GroupClient groupClient;
    private final DatePlanAccessPolicy datePlanAccessPolicy;

    @Transactional
    public DatePlanResponse create(UUID createdBy, CreateDatePlanRequest request) {
        DatePlan datePlan = datePlanMapper.toEntity(request, createdBy);
        datePlan.setGroupId(resolveGroupId(createdBy, request.getGroupId()));
        return datePlanMapper.toResponse(datePlanRepository.save(datePlan));
    }

    @Transactional(readOnly = true)
    public DatePlanResponse getById(UUID currentUserId, UUID id) {
        DatePlan datePlan = findById(id);
        datePlanAccessPolicy.assertCanView(datePlan, currentUserId);
        return datePlanMapper.toResponse(datePlan);
    }

    @Transactional(readOnly = true)
    public List<DatePlanResponse> getByGroupId(UUID currentUserId, UUID groupId) {
        if (!groupClient.isUserInGroup(currentUserId, groupId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not part of this group");
        }
        return datePlanRepository.findByGroupId(groupId).stream()
                .sorted(Comparator.comparing(
                        DatePlan::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(datePlanMapper::toResponse)
                .toList();
    }

    @Transactional
    public DatePlanResponse update(UUID currentUserId, UUID id, UpdateDatePlanRequest request) {
        DatePlan datePlan = findById(id);
        datePlanAccessPolicy.assertCanEdit(datePlan, currentUserId);
        datePlanMapper.updateEntity(datePlan, request);
        return datePlanMapper.toResponse(datePlanRepository.save(datePlan));
    }

    @Transactional
    public void delete(UUID currentUserId, UUID id) {
        DatePlan datePlan = findById(id);
        datePlanAccessPolicy.assertCanEdit(datePlan, currentUserId);
        datePlanRepository.delete(datePlan);
    }

    private DatePlan findById(UUID id) {
        return datePlanRepository.findById(id)
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
