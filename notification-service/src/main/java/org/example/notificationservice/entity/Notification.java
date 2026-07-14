package org.example.notificationservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "notifications",
        indexes = {
                @Index(name = "idx_notifications_recipient_created_at", columnList = "recipient_user_id, created_at"),
                @Index(name = "idx_notifications_recipient_status", columnList = "recipient_user_id, status"),
                @Index(name = "idx_notifications_source_event_id", columnList = "source_event_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unc_source_id_recipient_id",  columnNames = {"source_event_id", "recipient_user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "recipient_user_id", nullable = false)
    private UUID recipientUserId;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private NotificationType type;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "resource_type", length = 80)
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "source_event_id")
    private UUID sourceEventId;

    @Column(name = "source_topic", length = 120)
    private String sourceTopic;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private NotificationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;
}
