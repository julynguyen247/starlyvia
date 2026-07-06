package org.example.dateplanservice.repository;

import org.example.dateplanservice.entity.CoupleMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CoupleMembershipRepository extends JpaRepository<CoupleMembership, UUID> {
    Optional<CoupleMembership> findFirstByUserIdOrPartnerId(UUID userId, UUID partnerId);

    boolean existsByUserIdAndPartnerId(UUID userId, UUID partnerId);
}
