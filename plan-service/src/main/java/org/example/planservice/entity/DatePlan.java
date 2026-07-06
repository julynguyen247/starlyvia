package org.example.planservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.example.planservice.enums.PlanStatus;
import org.hibernate.engine.spi.CascadeStyle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DatePlan {

    @GeneratedValue(strategy = GenerationType.UUID)
    @Id
    private UUID id;

    @NotNull
    private String planName;

    @NotNull
    private String planDescription;

    @NotNull
    private LocalDate planStartDate;
    @NotNull
    private LocalDate planEndDate;

    @NotNull
    private LocalTime planStartTime;

    @NotNull
    private LocalTime planEndTime;

    private UUID groupId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PlanStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private UUID createdBy;

    @OneToMany(
        mappedBy = "datePlan",
        cascade= CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlanStop> stops=new ArrayList<>();

}
