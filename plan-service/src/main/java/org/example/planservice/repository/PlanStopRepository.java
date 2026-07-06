package org.example.planservice.repository;

import org.example.planservice.entity.PlanStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanStopRepository extends JpaRepository<PlanStop, UUID> {
    List<PlanStop> findByPlanId(UUID planId);
}
