package org.example.notificationservice.repository;

import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.entity.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByRecipientUserId(UUID recipientUserId, Pageable pageable);

    List<Notification> findByRecipientUserIdAndStatus(UUID recipientUserId, NotificationStatus status);

    Optional<Notification> findByIdAndRecipientUserId(UUID id, UUID recipientUserId);

    long countByRecipientUserIdAndStatus(UUID recipientUserId, NotificationStatus status);

    Optional<Notification> findBySourceEventIdAndRecipientUserId(UUID sourceEventId, UUID recipientUserId);
}
