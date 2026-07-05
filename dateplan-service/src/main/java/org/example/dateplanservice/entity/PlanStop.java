package org.example.dateplanservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanStop {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    private String name;

    private String address;

    private Double latitude;

    private Double longitude;

    private Integer orderIndex;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_plan_id", nullable = false)
    private DatePlan datePlan;
}