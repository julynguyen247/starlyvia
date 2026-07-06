package org.example.coupleservice.service;

import lombok.RequiredArgsConstructor;
import org.example.coupleservice.client.UserClient;
import org.example.coupleservice.entity.Couple;
import org.example.coupleservice.entity.CoupleRequest;
import org.example.coupleservice.entity.CoupleRequestStatus;
import org.example.coupleservice.event.CoupleAcceptedEvent;
import org.example.coupleservice.event.CoupleRemovedEvent;
import org.example.coupleservice.event.CoupleRequestEvent;
import org.example.coupleservice.event.DomainEventPublisher;
import org.example.coupleservice.repository.CoupleRepository;
import org.example.coupleservice.repository.CoupleRequestRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoupleService {
    private static final int COUPLE_REQUEST_EVENT_VERSION = 1;
    private static final int COUPLE_ACCEPTED_EVENT_VERSION = 1;
    private static final int COUPLE_REMOVED_EVENT_VERSION = 1;

    private final CoupleRequestRepository coupleRequestRepository;
    private final CoupleRepository coupleRepository;
    private final UserClient userClient;
    private final DomainEventPublisher eventPublisher;

    @Value("${app.kafka.topics.couple-requested:couple.requested}")
    private String coupleRequestedTopic;

    @Value("${app.kafka.topics.couple-accepted:couple.accepted}")
    private String coupleAcceptedTopic;

    @Value("${app.kafka.topics.couple-rejected:couple.rejected}")
    private String coupleRejectedTopic;

    @Value("${app.kafka.topics.couple-removed:couple.removed}")
    private String coupleRemovedTopic;

    @Transactional
    public CoupleRequest sendRequest(UUID requesterId, UUID receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot send a couple request to yourself");
        }
        if (!userClient.exists(receiverId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Receiver user not found");
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
            CoupleRequest savedRequest = coupleRequestRepository.save(existingRequest);
            publishCoupleRequestEvent(coupleRequestedTopic, "couple.requested", savedRequest);
            return savedRequest;
        }

        CoupleRequest request = CoupleRequest.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .status(CoupleRequestStatus.PENDING)
                .build();

        CoupleRequest savedRequest = coupleRequestRepository.save(request);
        publishCoupleRequestEvent(coupleRequestedTopic, "couple.requested", savedRequest);
        return savedRequest;
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

        Couple couple = coupleRepository.findByUserIdAndPartnerId(firstUserId, secondUserId)
                .orElseGet(() -> coupleRepository.save(Couple.builder()
                        .userId(firstUserId)
                        .partnerId(secondUserId)
                        .build()));
        publishCoupleAccepted(request, couple);
        return couple;
    }

    @Transactional
    public CoupleRequest rejectRequest(UUID currentUserId, UUID requestId) {
        CoupleRequest request = findOwnedPendingRequest(currentUserId, requestId);
        request.setStatus(CoupleRequestStatus.REJECTED);
        CoupleRequest savedRequest = coupleRequestRepository.save(request);
        publishCoupleRequestEvent(coupleRejectedTopic, "couple.rejected", savedRequest);
        return savedRequest;
    }

    @Transactional
    public void removeCouple(UUID currentUserId, UUID partnerId) {
        UUID firstUserId = min(currentUserId, partnerId);
        UUID secondUserId = max(currentUserId, partnerId);

        Couple couple = coupleRepository.findByUserIdAndPartnerId(firstUserId, secondUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Couple not found"));
        coupleRepository.delete(couple);
        publishCoupleRemoved(couple);
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

    private void publishCoupleRequestEvent(String topic, String eventType, CoupleRequest request) {
        CoupleRequestEvent payload = new CoupleRequestEvent(
                UUID.randomUUID(),
                eventType,
                COUPLE_REQUEST_EVENT_VERSION,
                LocalDateTime.now().toString(),
                request.getId(),
                request.getRequesterId(),
                request.getReceiverId(),
                request.getStatus().name()
        );
        eventPublisher.publish(topic, request.getId().toString(), payload);
    }

    private void publishCoupleAccepted(CoupleRequest request, Couple couple) {
        CoupleAcceptedEvent payload = new CoupleAcceptedEvent(
                UUID.randomUUID(),
                "couple.accepted",
                COUPLE_ACCEPTED_EVENT_VERSION,
                LocalDateTime.now().toString(),
                request.getId(),
                couple.getId(),
                request.getRequesterId(),
                request.getReceiverId(),
                couple.getUserId(),
                couple.getPartnerId()
        );
        eventPublisher.publish(coupleAcceptedTopic, couple.getId().toString(), payload);
    }

    private void publishCoupleRemoved(Couple couple) {
        CoupleRemovedEvent payload = new CoupleRemovedEvent(
                UUID.randomUUID(),
                "couple.removed",
                COUPLE_REMOVED_EVENT_VERSION,
                LocalDateTime.now().toString(),
                couple.getId(),
                couple.getUserId(),
                couple.getPartnerId()
        );
        eventPublisher.publish(coupleRemovedTopic, couple.getId().toString(), payload);
    }
}
