package org.example.authservice.grpcserver;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.example.authservice.entity.User;
import org.example.authservice.grpc.GetUserRequest;
import org.example.authservice.grpc.UserExistsRequest;
import org.example.authservice.grpc.UserExistsResponse;
import org.example.authservice.grpc.UserResponse;
import org.example.authservice.grpc.UserServiceGrpc;
import org.example.authservice.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    private final UserRepository userRepository;

    @Override
    public void userExists(
            UserExistsRequest request,
            StreamObserver<UserExistsResponse> responseObserver
    ) {
        UUID userId = parseUserId(request.getUserId(), responseObserver);
        if (userId == null) {
            return;
        }

        responseObserver.onNext(UserExistsResponse.newBuilder()
                .setExists(userRepository.existsById(userId))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getUser(
            GetUserRequest request,
            StreamObserver<UserResponse> responseObserver
    ) {
        UUID userId = parseUserId(request.getUserId(), responseObserver);
        if (userId == null) {
            return;
        }

        Optional<User> user = userRepository.findById(userId);
        responseObserver.onNext(user.map(this::toResponse)
                .orElseGet(() -> UserResponse.newBuilder().setExists(false).build()));
        responseObserver.onCompleted();
    }

    private UserResponse toResponse(User user) {
        UserResponse.Builder builder = UserResponse.newBuilder()
                .setExists(true)
                .setUserId(user.getId().toString())
                .setEmail(user.getEmail())
                .setUsername(user.getUsername())
                .setRole(user.getRole());

        if (user.getAvatarUrl() != null) {
            builder.setAvatarUrl(user.getAvatarUrl());
        }
        if (user.getBio() != null) {
            builder.setBio(user.getBio());
        }

        return builder.build();
    }

    private UUID parseUserId(String rawUserId, StreamObserver<?> responseObserver) {
        try {
            return UUID.fromString(rawUserId);
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription("user_id must be a valid UUID")
                    .asRuntimeException());
            return null;
        }
    }
}
