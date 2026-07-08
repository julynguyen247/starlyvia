package org.example.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.CreateNotificationRequest;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.dto.UnreadCountResponse;
import org.example.notificationservice.dto.UpdateNotificationRequest;
import org.example.notificationservice.service.NotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationResponse create(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody CreateNotificationRequest request
    ) {
        return notificationService.create(currentUserId, request);
    }

    @GetMapping
    public List<NotificationResponse> getMyNotifications(@RequestHeader("X-User-Id") UUID currentUserId) {
        return notificationService.getMyNotifications(currentUserId);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(@RequestHeader("X-User-Id") UUID currentUserId) {
        return new UnreadCountResponse(notificationService.countUnread(currentUserId));
    }

    @GetMapping("/{id}")
    public NotificationResponse getById(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id
    ) {
        return notificationService.getById(currentUserId, id);
    }

    @PutMapping("/{id}")
    public NotificationResponse update(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateNotificationRequest request
    ) {
        return notificationService.update(currentUserId, id, request);
    }

    @PatchMapping("/{id}/read")
    public NotificationResponse markRead(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id
    ) {
        return notificationService.markRead(currentUserId, id);
    }

    @PatchMapping("/read-all")
    public List<NotificationResponse> markAllRead(@RequestHeader("X-User-Id") UUID currentUserId) {
        return notificationService.markAllRead(currentUserId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID id
    ) {
        notificationService.delete(currentUserId, id);
    }
}
