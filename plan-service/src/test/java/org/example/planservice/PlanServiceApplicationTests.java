package org.example.planservice;

import org.example.planservice.entity.Plan;
import org.example.planservice.entity.PlanStop;
import org.example.planservice.enums.PlanStatus;
import org.example.planservice.repository.PlanRepository;
import org.example.planservice.repository.PlanStopRepository;
import org.example.planservice.service.GroupPlanCleanupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PlanServiceApplicationTests {

    @Autowired
    private GroupPlanCleanupService cleanupService;

    @Autowired
    private PlanRepository planRepository;

    @Autowired
    private PlanStopRepository planStopRepository;

    @BeforeEach
    void cleanDatabase() {
        planStopRepository.deleteAll();
        planRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void groupDeletionRemovesPlansAndStops() {
        UUID groupId = UUID.randomUUID();
        Plan plan = Plan.builder()
                .planName("Farewell Tour")
                .planDescription("One last route")
                .planStartDate(LocalDate.now())
                .planEndDate(LocalDate.now())
                .planStartTime(LocalTime.of(9, 0))
                .planEndTime(LocalTime.of(18, 0))
                .groupId(groupId)
                .status(PlanStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy(UUID.randomUUID())
                .stops(new ArrayList<>())
                .build();
        plan.getStops().add(PlanStop.builder()
                .name("Station")
                .orderIndex(0)
                .plan(plan)
                .build());
        planRepository.saveAndFlush(plan);

        long deletedPlans = cleanupService.deleteByGroupId(groupId);

        assertThat(deletedPlans).isEqualTo(1);
        assertThat(planRepository.findByGroupId(groupId)).isEmpty();
        assertThat(planStopRepository.findAll()).isEmpty();
    }
}
