package org.example.groupservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record GroupJoinCodeResponse(
        UUID token,
        UUID groupId,
        String groupName,
        LocalDateTime expiresAt
) {
}
