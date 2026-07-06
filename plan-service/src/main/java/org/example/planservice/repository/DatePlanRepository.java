package org.example.planservice.repository;

import org.example.planservice.entity.DatePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DatePlanRepository extends JpaRepository<DatePlan, UUID> {
    List<DatePlan> findByGroupId(UUID groupId);
}
