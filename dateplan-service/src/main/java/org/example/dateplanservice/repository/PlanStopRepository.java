package org.example.dateplanservice.repository;

import org.example.dateplanservice.entity.PlanStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PlanStopRepository extends JpaRepository<PlanStop, UUID> {
    List<PlanStop> findByDatePlanId(UUID datePlanId);
}
