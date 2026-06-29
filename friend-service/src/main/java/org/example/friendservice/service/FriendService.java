package org.example.friendservice.service;

import lombok.RequiredArgsConstructor;
import org.example.friendservice.entity.FriendRequest;
import org.example.friendservice.entity.FriendRequestStatus;
import org.example.friendservice.entity.Friendship;
import org.example.friendservice.repository.FriendRequestRepository;
import org.example.friendservice.repository.FriendshipRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FriendService {
    private final FriendRequestRepository friendRequestRepository;
    private final FriendshipRepository friendshipRepository;

    @Transactional
    public FriendRequest sendRequest(UUID requesterId, UUID receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send a friend request to yourself");
        }
        if (friendshipRepository.existsBetween(requesterId, receiverId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Users are already friends");
        }
        if (hasPendingRequestBetween(requesterId, receiverId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending friend request already exists");
        }

        FriendRequest existingRequest = friendRequestRepository
                .findByRequesterIdAndReceiverId(requesterId, receiverId)
                .orElse(null);
        if (existingRequest != null) {
            existingRequest.setStatus(FriendRequestStatus.PENDING);
            return friendRequestRepository.save(existingRequest);
        }

        FriendRequest request = FriendRequest.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status(FriendRequestStatus.PENDING)
                .build();

        return friendRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<FriendRequest> incomingRequests(UUID userId) {
        return friendRequestRepository.findByReceiverId(userId);
    }

    @Transactional(readOnly = true)
    public List<FriendRequest> outgoingRequests(UUID userId) {
        return friendRequestRepository.findByRequesterId(userId);
    }

    @Transactional(readOnly = true)
    public List<Friendship> friendships(UUID userId) {
        return friendshipRepository.findByUserIdOrFriendId(userId, userId);
    }

    @Transactional
    public Friendship acceptRequest(UUID currentUserId, UUID requestId) {
        FriendRequest request = findOwnedPendingRequest(currentUserId, requestId);
        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        UUID firstUserId = min(request.getRequesterId(), request.getReceiverId());
        UUID secondUserId = max(request.getRequesterId(), request.getReceiverId());

        return friendshipRepository.findByUserIdAndFriendId(firstUserId, secondUserId)
                .orElseGet(() -> friendshipRepository.save(Friendship.builder()
                        .userId(firstUserId)
                        .friendId(secondUserId)
                        .build()));
    }

    @Transactional
    public FriendRequest rejectRequest(UUID currentUserId, UUID requestId) {
        FriendRequest request = findOwnedPendingRequest(currentUserId, requestId);
        request.setStatus(FriendRequestStatus.REJECTED);
        return friendRequestRepository.save(request);
    }

    @Transactional
    public void removeFriend(UUID currentUserId, UUID friendId) {
        UUID firstUserId = min(currentUserId, friendId);
        UUID secondUserId = max(currentUserId, friendId);

        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(firstUserId, secondUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found"));
        friendshipRepository.delete(friendship);
    }

    private boolean hasPendingRequestBetween(UUID firstUserId, UUID secondUserId) {
        return friendRequestRepository.existsByRequesterIdAndReceiverIdAndStatus(
                firstUserId,
                secondUserId,
                FriendRequestStatus.PENDING
        ) || friendRequestRepository.existsByRequesterIdAndReceiverIdAndStatus(
                secondUserId,
                firstUserId,
                FriendRequestStatus.PENDING
        );
    }

    private FriendRequest findOwnedPendingRequest(UUID currentUserId, UUID requestId) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (!request.getReceiverId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the receiver can update this request");
        }
        if (request.getStatus() != FriendRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request is not pending");
        }

        return request;
    }

    private UUID min(UUID firstUserId, UUID secondUserId) {
        return Comparator.<UUID>naturalOrder().compare(firstUserId, secondUserId) <= 0
                ? firstUserId
                : secondUserId;
    }

    private UUID max(UUID firstUserId, UUID secondUserId) {
        return Comparator.<UUID>naturalOrder().compare(firstUserId, secondUserId) >= 0
                ? firstUserId
                : secondUserId;
    }
}
