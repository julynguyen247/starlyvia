package org.example.groupservice.dto;

import org.example.groupservice.entity.GroupType;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupJoinPreviewResponse(
        UUID groupId,
        String groupName,
        String groupDescription,
        GroupType groupType,
        LocalDateTime expiresAt,
        boolean alreadyMember
) {
}
