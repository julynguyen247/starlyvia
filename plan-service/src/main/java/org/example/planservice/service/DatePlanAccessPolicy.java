package org.example.planservice.service;

import lombok.RequiredArgsConstructor;
import org.example.planservice.client.GroupClient;
import org.example.planservice.entity.DatePlan;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DatePlanAccessPolicy {
    private final GroupClient groupClient;

    public void assertCanView(DatePlan datePlan, UUID currentUserId) {
        if (datePlan.getCreatedBy().equals(currentUserId)) {
            return;
        }
        if (datePlan.getGroupId() != null && groupClient.isUserInGroup(currentUserId, datePlan.getGroupId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator or group members can view this date plan");
    }

    public void assertCanEdit(DatePlan datePlan, UUID currentUserId) {
        if (datePlan.getCreatedBy().equals(currentUserId)) {
            return;
        }
        if (datePlan.getGroupId() != null && groupClient.isUserInGroup(currentUserId, datePlan.getGroupId())) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the creator or group members can edit this date plan");
    }
}
