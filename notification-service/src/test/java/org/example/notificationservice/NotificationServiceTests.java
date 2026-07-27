package org.example.notificationservice;

import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.repository.NotificationRepository;
import org.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class NotificationServiceTests {
    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    void cleanDatabase() {
        notificationRepository.deleteAll();
    }

    @Test
    void createAddsUnreadNotificationForCurrentUser() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        NotificationResponse response = createFromEvent(eventId, userId, "Invite", "You have an invite");

        assertThat(response.recipientUserId()).isEqualTo(userId);
        assertThat(response.sourceEventId()).isEqualTo(eventId);
        assertThat(response.title()).isEqualTo("Invite");
        assertThat(response.createdAt()).isNotNull();
        assertThat(notificationService.countUnread(userId)).isEqualTo(1);
        assertThat(notificationService.getMyNotifications(userId, firstPage()).getContent()).hasSize(1);
    }

    @Test
    void createFromEventIsIdempotentBySourceEventAndRecipient() {
        UUID userId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();

        NotificationResponse first = createFromEvent(eventId, userId, "Invite", "You have an invite");
        NotificationResponse second = createFromEvent(eventId, userId, "Changed", "Duplicate event");

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(second.title()).isEqualTo("Invite");
        assertThat(notificationRepository.count()).isEqualTo(1);
    }

    @Test
    void sameEventCreatesNotificationsForDifferentRecipients() {
        UUID eventId = UUID.randomUUID();
        UUID firstUserId = UUID.randomUUID();
        UUID secondUserId = UUID.randomUUID();

        createFromEvent(eventId, firstUserId, "Plan updated", "A plan was updated");
        createFromEvent(eventId, secondUserId, "Plan updated", "A plan was updated");

        assertThat(notificationRepository.count()).isEqualTo(2);
        assertThat(notificationService.countUnread(firstUserId)).isEqualTo(1);
        assertThat(notificationService.countUnread(secondUserId)).isEqualTo(1);
    }

    @Test
    void markReadDecrementsUnreadCount() {
        UUID userId = UUID.randomUUID();
        NotificationResponse response = createFromEvent(UUID.randomUUID(), userId, "Invite", "You have an invite");

        NotificationResponse read = notificationService.markRead(userId, response.id());

        assertThat(read.readAt()).isNotNull();
        assertThat(notificationService.countUnread(userId)).isZero();
    }

    @Test
    void userCannotReadAnotherUsersNotification() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        NotificationResponse response = createFromEvent(UUID.randomUUID(), ownerId, "Invite", "You have an invite");

        assertThatThrownBy(() -> notificationService.getById(otherUserId, response.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Notification not found");
    }

    private NotificationResponse createFromEvent(UUID eventId, UUID userId, String title, String message) {
        return notificationService.createFromEvent(
                eventId,
                "group.events",
                userId,
                UUID.randomUUID(),
                NotificationType.GROUP_INVITATION_CREATED,
                title,
                message,
                "GROUP",
                UUID.randomUUID()
        );
    }

    private PageRequest firstPage() {
        return PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
}
