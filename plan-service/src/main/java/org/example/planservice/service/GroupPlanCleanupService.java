package org.example.planservice.service;

import lombok.RequiredArgsConstructor;
import org.example.planservice.repository.PlanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupPlanCleanupService {
    private final PlanRepository planRepository;

    @Transactional
    public long deleteByGroupId(UUID groupId) {
        return planRepository.deleteByGroupId(groupId);
    }
}
