package org.example.coupleservice.client;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.example.coupleservice.grpc.auth.UserExistsRequest;
import org.example.coupleservice.grpc.auth.UserServiceGrpc;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrpcUserClient implements UserClient {
    private final UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub;

    @Override
    public boolean exists(UUID userId) {
        try {
            return userServiceBlockingStub.userExists(UserExistsRequest.newBuilder()
                            .setUserId(userId.toString())
                            .build())
                    .getExists();
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.Code.INVALID_ARGUMENT) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getStatus().getDescription(), ex);
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Auth service is unavailable", ex);
        }
    }
}
