package org.example.coupleservice.client;

import java.util.UUID;

public interface UserClient {
    boolean exists(UUID userId);
}
