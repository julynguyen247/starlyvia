package org.example.groupservice.repository;

import org.example.groupservice.entity.PlanGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PlanGroupRepository extends JpaRepository<PlanGroup, UUID> {
    @Query("""
            select g
            from PlanGroup g
            join GroupMember m on m.group = g
            where m.userId = :userId
            order by g.updatedAt desc
            """)
    List<PlanGroup> findByMemberUserId(@Param("userId") UUID userId);
}
