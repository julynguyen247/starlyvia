package org.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.CreateNotificationRequest;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.dto.UpdateNotificationRequest;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.entity.NotificationStatus;
import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.mapper.NotificationMapper;
import org.example.notificationservice.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationResponse create(UUID currentUserId, CreateNotificationRequest request) {
        Notification notification = notificationMapper.toEntity(currentUserId, request);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse createFromEvent(
            UUID recipientUserId,
            UUID actorUserId,
            NotificationType type,
            String title,
            String message,
            String resourceType,
            UUID resourceId
    ){
        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .actorUserId(actorUserId)
                .type(type)
                .title(title)
                .message(message)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .status(NotificationStatus.UNREAD)
                .build();
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(UUID currentUserId) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public NotificationResponse getById(UUID currentUserId, UUID id) {
        return notificationMapper.toResponse(findOwnedNotification(currentUserId, id));
    }

    @Transactional(readOnly = true)
    public long countUnread(UUID currentUserId) {
        return notificationRepository.countByRecipientUserIdAndStatus(currentUserId, NotificationStatus.UNREAD);
    }

    @Transactional
    public NotificationResponse update(UUID currentUserId, UUID id, UpdateNotificationRequest request) {
        Notification notification = findOwnedNotification(currentUserId, id);
        notificationMapper.updateEntity(notification, request);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public NotificationResponse markRead(UUID currentUserId, UUID id) {
        Notification notification = findOwnedNotification(currentUserId, id);
        if (notification.getStatus() != NotificationStatus.READ) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(LocalDateTime.now());
        }
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public List<NotificationResponse> markAllRead(UUID currentUserId) {
        List<Notification> unreadNotifications = notificationRepository.findByRecipientUserIdAndStatus(
                currentUserId,
                NotificationStatus.UNREAD
        );
        LocalDateTime readAt = LocalDateTime.now();
        unreadNotifications.forEach(notification -> {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(readAt);
        });
        return notificationRepository.saveAll(unreadNotifications).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public void delete(UUID currentUserId, UUID id) {
        Notification notification = findOwnedNotification(currentUserId, id);
        notificationRepository.delete(notification);
    }

    private Notification findOwnedNotification(UUID currentUserId, UUID id) {
        return notificationRepository.findByIdAndRecipientUserId(id, currentUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
    }
}
