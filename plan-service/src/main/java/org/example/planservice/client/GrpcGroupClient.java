package org.example.planservice.client;

import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.example.planservice.grpc.group.GetGroupMemberIdsRequest;
import org.example.planservice.grpc.group.GroupQueryServiceGrpc;
import org.example.planservice.grpc.group.IsUserInGroupRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrpcGroupClient implements GroupClient {
    private final GroupQueryServiceGrpc.GroupQueryServiceBlockingStub groupQueryServiceBlockingStub;

    @Override
    public boolean isUserInGroup(UUID userId, UUID groupId) {
        try {
            return groupQueryServiceBlockingStub.isUserInGroup(
                    IsUserInGroupRequest.newBuilder()
                            .setUserId(userId.toString())
                            .setGroupId(groupId.toString())
                            .build()
            ).getIsMember();
        } catch (StatusRuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Group service is unavailable", ex);
        }
    }

    @Override
    public List<UUID> getGroupMemberIds(UUID groupId) {
        try {
            return groupQueryServiceBlockingStub.getGroupMemberIds(
                            GetGroupMemberIdsRequest.newBuilder()
                                    .setGroupId(groupId.toString())
                                    .build()
                    ).getUserIdsList().stream()
                    .map(UUID::fromString)
                    .toList();
        } catch (StatusRuntimeException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Group service is unavailable", ex);
        }
    }
}
