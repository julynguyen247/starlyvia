package org.example.groupservice.grpcserver;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.example.groupservice.grpc.group.GetGroupMemberIdsRequest;
import org.example.groupservice.grpc.group.GroupMemberIdsResponse;
import org.example.groupservice.grpc.group.GroupMembershipResponse;
import org.example.groupservice.grpc.group.GroupQueryServiceGrpc;
import org.example.groupservice.grpc.group.IsUserInGroupRequest;
import org.example.groupservice.repository.GroupMemberRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GroupQueryGrpcService extends GroupQueryServiceGrpc.GroupQueryServiceImplBase {
    private final GroupMemberRepository groupMemberRepository;

    @Override
    public void isUserInGroup(
            IsUserInGroupRequest request,
            StreamObserver<GroupMembershipResponse> responseObserver
    ) {
        UUID userId = parseUuid("user_id", request.getUserId(), responseObserver);
        UUID groupId = parseUuid("group_id", request.getGroupId(), responseObserver);
        if (userId == null || groupId == null) {
            return;
        }

        responseObserver.onNext(GroupMembershipResponse.newBuilder()
                .setIsMember(groupMemberRepository.existsByGroupIdAndUserId(groupId, userId))
                .build());
        responseObserver.onCompleted();
    }

    @Override
    public void getGroupMemberIds(
            GetGroupMemberIdsRequest request,
            StreamObserver<GroupMemberIdsResponse> responseObserver
    ) {
        UUID groupId = parseUuid("group_id", request.getGroupId(), responseObserver);
        if (groupId == null) {
            return;
        }

        GroupMemberIdsResponse response = GroupMemberIdsResponse.newBuilder()
                .addAllUserIds(groupMemberRepository.findByGroupId(groupId).stream()
                        .map(member -> member.getUserId().toString())
                        .toList())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private UUID parseUuid(String fieldName, String rawValue, StreamObserver<?> responseObserver) {
        try {
            return UUID.fromString(rawValue);
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT
                    .withDescription(fieldName + " must be a valid UUID")
                    .asRuntimeException());
            return null;
        }
    }
}
