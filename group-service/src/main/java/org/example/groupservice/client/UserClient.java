package org.example.groupservice.client;

import java.util.UUID;

public interface UserClient {
    boolean exists(UUID userId);
}
