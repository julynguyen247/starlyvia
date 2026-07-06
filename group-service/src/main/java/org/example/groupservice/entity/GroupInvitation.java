package org.example.groupservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "group_invitations",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_group_invitations_group_invitee",
                        columnNames = {"group_id", "invitee_id"}
                )
        },
        indexes = {
                @Index(name = "idx_group_invitations_invitee_id", columnList = "invitee_id"),
                @Index(name = "idx_group_invitations_group_id", columnList = "group_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PlanGroup group;

    @Column(name = "inviter_id", nullable = false)
    private UUID inviterId;

    @Column(name = "invitee_id", nullable = false)
    private UUID inviteeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupInvitationStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
