package org.example.dateplanservice.service;

import lombok.RequiredArgsConstructor;
import org.example.dateplanservice.entity.DatePlan;
import org.example.dateplanservice.repository.CoupleMembershipRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DatePlanAccessPolicy {
    private final CoupleMembershipRepository coupleMembershipRepository;

    public void assertCanEdit(DatePlan datePlan, UUID currentUserId) {
        if (datePlan.getCreatedBy().equals(currentUserId) || isCreatorPartner(datePlan.getCreatedBy(), currentUserId)) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator or current partner can edit this date plan");
    }

    private boolean isCreatorPartner(UUID creatorId, UUID currentUserId) {
        return coupleMembershipRepository.existsByUserIdAndPartnerId(creatorId, currentUserId)
                || coupleMembershipRepository.existsByUserIdAndPartnerId(currentUserId, creatorId);
    }
}
