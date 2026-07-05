package org.example.coupleservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "couples",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_couples_user_partner",
                        columnNames = {"user_id", "partner_id"}
                )
        },
        indexes = {
                @Index(name = "idx_couples_user_id", columnList = "user_id"),
                @Index(name = "idx_couples_partner_id", columnList = "partner_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Couple {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "partner_id", nullable = false)
    private UUID partnerId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
