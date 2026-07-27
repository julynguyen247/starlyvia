package org.example.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.Notification;
import org.example.notificationservice.entity.NotificationStatus;
import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.event.NotificationCreatedEvent;
import org.example.notificationservice.mapper.NotificationMapper;
import org.example.notificationservice.repository.NotificationRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public NotificationResponse createFromEvent(
            UUID sourceEventId,
            String sourceTopic,
            UUID recipientUserId,
            UUID actorUserId,
            NotificationType type,
            String title,
            String message,
            String resourceType,
        UUID resourceId
    ){
        if (sourceEventId != null) {
            return notificationRepository.findBySourceEventIdAndRecipientUserId(sourceEventId, recipientUserId)
                    .map(notificationMapper::toResponse)
                    .orElseGet(() -> createEventNotification(
                            sourceEventId,
                            sourceTopic,
                            recipientUserId,
                            actorUserId,
                            type,
                            title,
                            message,
                            resourceType,
                            resourceId
                    ));
        }

        return createEventNotification(
                null,
                sourceTopic,
                recipientUserId,
                actorUserId,
                type,
                title,
                message,
                resourceType,
                resourceId
        );
    }

    private NotificationResponse createEventNotification(
            UUID sourceEventId,
            String sourceTopic,
            UUID recipientUserId,
            UUID actorUserId,
            NotificationType type,
            String title,
            String message,
            String resourceType,
            UUID resourceId
    ) {
        Notification notification = Notification.builder()
                .recipientUserId(recipientUserId)
                .actorUserId(actorUserId)
                .type(type)
                .title(title)
                .message(message)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .sourceEventId(sourceEventId)
                .sourceTopic(sourceTopic)
                .status(NotificationStatus.UNREAD)
                .build();
        try {
            NotificationResponse response = notificationMapper.toResponse(notificationRepository.saveAndFlush(notification));
            applicationEventPublisher.publishEvent(new NotificationCreatedEvent(response));
            return response;
        } catch (DataIntegrityViolationException ex) {
            if (sourceEventId == null) {
                throw ex;
            }
            return notificationRepository.findBySourceEventIdAndRecipientUserId(sourceEventId, recipientUserId)
                    .map(notificationMapper::toResponse)
                    .orElseThrow(() -> ex);
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationResponse> getMyNotifications(UUID currentUserId, Pageable pageable) {
        return notificationRepository.findByRecipientUserId(currentUserId, pageable)
                .map(notificationMapper::toResponse);
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
