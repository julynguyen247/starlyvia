package org.example.friendservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.friendservice.dto.CreateFriendRequest;
import org.example.friendservice.entity.FriendRequest;
import org.example.friendservice.entity.Friendship;
import org.example.friendservice.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {
    private final FriendService friendService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public FriendRequest sendRequest(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody CreateFriendRequest request
    ) {
        return friendService.sendRequest(currentUserId, request.getReceiverId());
    }

    @GetMapping("/requests/incoming")
    public List<FriendRequest> incomingRequests(@RequestHeader("X-User-Id") UUID currentUserId) {
        return friendService.incomingRequests(currentUserId);
    }

    @GetMapping("/requests/outgoing")
    public List<FriendRequest> outgoingRequests(@RequestHeader("X-User-Id") UUID currentUserId) {
        return friendService.outgoingRequests(currentUserId);
    }

    @PostMapping("/requests/{requestId}/accept")
    public Friendship acceptRequest(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID requestId
    ) {
        return friendService.acceptRequest(currentUserId, requestId);
    }

    @PostMapping("/requests/{requestId}/reject")
    public FriendRequest rejectRequest(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID requestId
    ) {
        return friendService.rejectRequest(currentUserId, requestId);
    }

    @GetMapping
    public List<Friendship> friendships(@RequestHeader("X-User-Id") UUID currentUserId) {
        return friendService.friendships(currentUserId);
    }

    @DeleteMapping("/{friendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFriend(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID friendId
    ) {
        friendService.removeFriend(currentUserId, friendId);
    }
}
