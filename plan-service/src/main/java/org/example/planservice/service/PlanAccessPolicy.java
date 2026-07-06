package org.example.planservice.service;

import lombok.RequiredArgsConstructor;
import org.example.planservice.client.GroupClient;
import org.example.planservice.entity.Plan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PlanAccessPolicy {
    private final GroupClient groupClient;

    public void assertCanView(Plan plan, UUID currentUserId) {
        if (plan.getCreatedBy().equals(currentUserId)) {
            return;
        }
        if (plan.getGroupId() != null && groupClient.isUserInGroup(currentUserId, plan.getGroupId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator or group members can view this plan");
    }

    public void assertCanEdit(Plan plan, UUID currentUserId) {
        if (plan.getCreatedBy().equals(currentUserId)) {
            return;
        }
        if (plan.getGroupId() != null && groupClient.isUserInGroup(currentUserId, plan.getGroupId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator or group members can edit this plan");
    }
}
