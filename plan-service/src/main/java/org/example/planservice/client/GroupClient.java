package org.example.planservice.client;

import java.util.UUID;

public interface GroupClient {
    boolean isUserInGroup(UUID userId, UUID groupId);
}
