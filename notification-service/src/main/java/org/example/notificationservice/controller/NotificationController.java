package org.example.notificationservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.dto.UnreadCountResponse;
import org.example.notificationservice.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private static final int MAX_PAGE_SIZE = 100;

    private final NotificationService notificationService;

    @GetMapping
    public Page<NotificationResponse> getMyNotifications(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        PageRequest pageRequest = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        return notificationService.getMyNotifications(currentUserId, pageRequest);
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
