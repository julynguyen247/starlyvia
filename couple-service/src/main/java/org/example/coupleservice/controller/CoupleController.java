package org.example.coupleservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.coupleservice.dto.CreateCoupleRequest;
import org.example.coupleservice.entity.Couple;
import org.example.coupleservice.entity.CoupleRequest;
import org.example.coupleservice.service.CoupleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/couples")
@RequiredArgsConstructor
public class CoupleController {
    private final CoupleService coupleService;

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public CoupleRequest sendRequest(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody CreateCoupleRequest request
    ) {
        return coupleService.sendRequest(currentUserId, request.getReceiverId());
    }

    @GetMapping("/requests/incoming")
    public List<CoupleRequest> incomingRequests(@RequestHeader("X-User-Id") UUID currentUserId) {
        return coupleService.incomingRequests(currentUserId);
    }

    @GetMapping("/requests/outgoing")
    public List<CoupleRequest> outgoingRequests(@RequestHeader("X-User-Id") UUID currentUserId) {
        return coupleService.outgoingRequests(currentUserId);
    }

    @PostMapping("/requests/{requestId}/accept")
    public Couple acceptRequest(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID requestId
    ) {
        return coupleService.acceptRequest(currentUserId, requestId);
    }

    @PostMapping("/requests/{requestId}/reject")
    public CoupleRequest rejectRequest(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID requestId
    ) {
        return coupleService.rejectRequest(currentUserId, requestId);
    }

    @GetMapping
    public List<Couple> couples(@RequestHeader("X-User-Id") UUID currentUserId) {
        return coupleService.couples(currentUserId);
    }

    @DeleteMapping("/{partnerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeCouple(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID partnerId
    ) {
        coupleService.removeCouple(currentUserId, partnerId);
    }
}
