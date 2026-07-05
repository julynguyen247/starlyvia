package org.example.dateplanservice.repository;

import org.example.dateplanservice.entity.DatePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DatePlanRepository extends JpaRepository<DatePlan, UUID> {
    List<DatePlan> findByCoupleId(UUID coupleId);
}
