package org.example.coupleservice.repository;

import org.example.coupleservice.entity.CoupleRequest;
import org.example.coupleservice.entity.CoupleRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoupleRequestRepository extends JpaRepository<CoupleRequest, UUID> {
    List<CoupleRequest> findByRequesterId(UUID requesterId);

    List<CoupleRequest> findByReceiverId(UUID receiverId);

    Optional<CoupleRequest> findByRequesterIdAndReceiverId(UUID requesterId, UUID receiverId);

    Optional<CoupleRequest> findByRequesterIdAndReceiverIdAndStatus(
            UUID requesterId,
            UUID receiverId,
            CoupleRequestStatus status
    );

    boolean existsByRequesterIdAndReceiverIdAndStatus(
            UUID requesterId,
            UUID receiverId,
            CoupleRequestStatus status
    );
}
