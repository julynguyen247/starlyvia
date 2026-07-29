package org.example.groupservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "group_join_codes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_group_join_codes_group", columnNames = "group_id"),
                @UniqueConstraint(name = "uk_group_join_codes_token", columnNames = "token")
        },
        indexes = {
                @Index(name = "idx_group_join_codes_token", columnList = "token")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupJoinCode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PlanGroup group;

    @Column(nullable = false)
    private UUID token;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
