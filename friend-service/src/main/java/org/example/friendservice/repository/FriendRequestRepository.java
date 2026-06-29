package org.example.friendservice.repository;

import org.example.friendservice.entity.FriendRequest;
import org.example.friendservice.entity.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, UUID> {
    List<FriendRequest> findByRequesterId(UUID requesterId);

    List<FriendRequest> findByReceiverId(UUID receiverId);

    Optional<FriendRequest> findByRequesterIdAndReceiverId(UUID requesterId, UUID receiverId);

    Optional<FriendRequest> findByRequesterIdAndReceiverIdAndStatus(
            UUID requesterId,
            UUID receiverId,
            FriendRequestStatus status
    );

    boolean existsByRequesterIdAndReceiverIdAndStatus(
            UUID requesterId,
            UUID receiverId,
            FriendRequestStatus status
    );
}
