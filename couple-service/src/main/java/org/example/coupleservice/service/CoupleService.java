package org.example.coupleservice.service;

import lombok.RequiredArgsConstructor;
import org.example.coupleservice.entity.Couple;
import org.example.coupleservice.entity.CoupleRequest;
import org.example.coupleservice.entity.CoupleRequestStatus;
import org.example.coupleservice.repository.CoupleRepository;
import org.example.coupleservice.repository.CoupleRequestRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoupleService {
    private final CoupleRequestRepository coupleRequestRepository;
    private final CoupleRepository coupleRepository;

    @Transactional
    public CoupleRequest sendRequest(UUID requesterId, UUID receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send a couple request to yourself");
        }
        if (coupleRepository.existsByUserIdOrPartnerId(requesterId, requesterId)
                || coupleRepository.existsByUserIdOrPartnerId(receiverId, receiverId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "One of the users is already in a couple");
        }
        if (hasPendingRequestBetween(requesterId, receiverId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending couple request already exists");
        }

        CoupleRequest existingRequest = coupleRequestRepository
                .findByRequesterIdAndReceiverId(requesterId, receiverId)
                .orElse(null);
        if (existingRequest != null) {
            existingRequest.setStatus(CoupleRequestStatus.PENDING);
            return coupleRequestRepository.save(existingRequest);
        }

        CoupleRequest request = CoupleRequest.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status(CoupleRequestStatus.PENDING)
                .build();

        return coupleRequestRepository.save(request);
    }

    @Transactional(readOnly = true)
    public List<CoupleRequest> incomingRequests(UUID userId) {
        return coupleRequestRepository.findByReceiverId(userId);
    }

    @Transactional(readOnly = true)
    public List<CoupleRequest> outgoingRequests(UUID userId) {
        return coupleRequestRepository.findByRequesterId(userId);
    }

    @Transactional(readOnly = true)
    public List<Couple> couples(UUID userId) {
        return coupleRepository.findByUserIdOrPartnerId(userId, userId);
    }

    @Transactional
    public Couple acceptRequest(UUID currentUserId, UUID requestId) {
        CoupleRequest request = findOwnedPendingRequest(currentUserId, requestId);
        if (coupleRepository.existsByUserIdOrPartnerId(request.getRequesterId(), request.getRequesterId())
                || coupleRepository.existsByUserIdOrPartnerId(request.getReceiverId(), request.getReceiverId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "One of the users is already in a couple");
        }
        request.setStatus(CoupleRequestStatus.ACCEPTED);
        coupleRequestRepository.save(request);

        UUID firstUserId = min(request.getRequesterId(), request.getReceiverId());
        UUID secondUserId = max(request.getRequesterId(), request.getReceiverId());

        return coupleRepository.findByUserIdAndPartnerId(firstUserId, secondUserId)
                .orElseGet(() -> coupleRepository.save(Couple.builder()
                        .userId(firstUserId)
                        .partnerId(secondUserId)
                        .build()));
    }

    @Transactional
    public CoupleRequest rejectRequest(UUID currentUserId, UUID requestId) {
        CoupleRequest request = findOwnedPendingRequest(currentUserId, requestId);
        request.setStatus(CoupleRequestStatus.REJECTED);
        return coupleRequestRepository.save(request);
    }

    @Transactional
    public void removeCouple(UUID currentUserId, UUID partnerId) {
        UUID firstUserId = min(currentUserId, partnerId);
        UUID secondUserId = max(currentUserId, partnerId);

        Couple couple = coupleRepository.findByUserIdAndPartnerId(firstUserId, secondUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple not found"));
        coupleRepository.delete(couple);
    }

    private boolean hasPendingRequestBetween(UUID firstUserId, UUID secondUserId) {
        return coupleRequestRepository.existsByRequesterIdAndReceiverIdAndStatus(
                firstUserId,
                secondUserId,
                CoupleRequestStatus.PENDING
        ) || coupleRequestRepository.existsByRequesterIdAndReceiverIdAndStatus(
                secondUserId,
                firstUserId,
                CoupleRequestStatus.PENDING
        );
    }

    private CoupleRequest findOwnedPendingRequest(UUID currentUserId, UUID requestId) {
        CoupleRequest request = coupleRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple request not found"));

        if (!request.getReceiverId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the receiver can update this request");
        }
        if (request.getStatus() != CoupleRequestStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Couple request is not pending");
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
