package org.example.friendservice;

import org.example.friendservice.entity.FriendRequest;
import org.example.friendservice.entity.FriendRequestStatus;
import org.example.friendservice.entity.Friendship;
import org.example.friendservice.repository.FriendRequestRepository;
import org.example.friendservice.repository.FriendshipRepository;
import org.example.friendservice.service.FriendService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class FriendServiceApplicationTests {

    @Autowired
    private FriendService friendService;

    @Autowired
    private FriendRequestRepository friendRequestRepository;

    @Autowired
    private FriendshipRepository friendshipRepository;

    @BeforeEach
    void cleanDatabase() {
        friendshipRepository.deleteAll();
        friendRequestRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void sendAndAcceptFriendRequestStoresOnlyUserIds() {
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        FriendRequest request = friendService.sendRequest(requesterId, receiverId);
        Friendship friendship = friendService.acceptRequest(receiverId, request.getId());
        FriendRequest acceptedRequest = friendRequestRepository.findById(request.getId()).orElseThrow();

        assertThat(request.getRequesterId()).isEqualTo(requesterId);
        assertThat(request.getReceiverId()).isEqualTo(receiverId);
        assertThat(acceptedRequest.getStatus()).isEqualTo(FriendRequestStatus.ACCEPTED);
        assertThat(friendshipRepository.existsBetween(requesterId, receiverId)).isTrue();
        assertThat(friendship.getUserId()).isIn(requesterId, receiverId);
        assertThat(friendship.getFriendId()).isIn(requesterId, receiverId);
    }

    @Test
    void rejectedRequestCanBeSentAgainWithoutCreatingUserTable() {
        UUID requesterId = UUID.randomUUID();
        UUID receiverId = UUID.randomUUID();

        FriendRequest request = friendService.sendRequest(requesterId, receiverId);
        friendService.rejectRequest(receiverId, request.getId());

        FriendRequest resentRequest = friendService.sendRequest(requesterId, receiverId);

        assertThat(resentRequest.getId()).isEqualTo(request.getId());
        assertThat(resentRequest.getStatus()).isEqualTo(FriendRequestStatus.PENDING);
        assertThat(friendRequestRepository.findAll()).hasSize(1);
    }
}
