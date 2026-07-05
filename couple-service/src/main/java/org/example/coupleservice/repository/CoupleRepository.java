package org.example.coupleservice.repository;

import org.example.coupleservice.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CoupleRepository extends JpaRepository<Couple, UUID> {
    List<Couple> findByUserIdOrPartnerId(UUID userId, UUID partnerId);

    Optional<Couple> findByUserIdAndPartnerId(UUID userId, UUID partnerId);

    boolean existsByUserIdOrPartnerId(UUID userId, UUID partnerId);

    @Query("""
            select count(c) > 0
            from Couple c
            where (c.userId = :firstUserId and c.partnerId = :secondUserId)
               or (c.userId = :secondUserId and c.partnerId = :firstUserId)
            """)
    boolean existsBetween(
            @Param("firstUserId") UUID firstUserId,
            @Param("secondUserId") UUID secondUserId
    );
}
