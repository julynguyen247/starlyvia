package org.example.dateplanservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoupleMembership {
    @Id
    private UUID coupleId;

    @NotNull
    private UUID userId;

    @NotNull
    private UUID partnerId;

    @NotNull
    private LocalDateTime updatedAt;
}
