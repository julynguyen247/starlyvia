package org.example.planservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalTime;
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

    private String provider;

    private String providerPlaceId;

    private String photoUrl;

    private Double rating;

    private Integer ratingCount;

    private String websiteUrl;

    private String phoneNumber;

    private Integer orderIndex;

    private LocalTime arrivalTime;

    private LocalTime departureTime;

    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;
}
