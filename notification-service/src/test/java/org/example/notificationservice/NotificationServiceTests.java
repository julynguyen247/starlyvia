package org.example.notificationservice;

import org.example.notificationservice.dto.CreateNotificationRequest;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.NotificationType;
import org.example.notificationservice.repository.NotificationRepository;
import org.example.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

        NotificationResponse response = notificationService.create(userId, request("Invite", "You have an invite"));

        assertThat(response.recipientUserId()).isEqualTo(userId);
        assertThat(response.title()).isEqualTo("Invite");
        assertThat(notificationService.countUnread(userId)).isEqualTo(1);
        assertThat(notificationService.getMyNotifications(userId)).hasSize(1);
    }

    @Test
    void markReadDecrementsUnreadCount() {
        UUID userId = UUID.randomUUID();
        NotificationResponse response = notificationService.create(userId, request("Invite", "You have an invite"));

        NotificationResponse read = notificationService.markRead(userId, response.id());

        assertThat(read.readAt()).isNotNull();
        assertThat(notificationService.countUnread(userId)).isZero();
    }

    @Test
    void userCannotReadAnotherUsersNotification() {
        UUID ownerId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        NotificationResponse response = notificationService.create(ownerId, request("Invite", "You have an invite"));

        assertThatThrownBy(() -> notificationService.getById(otherUserId, response.id()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Notification not found");
    }

    private CreateNotificationRequest request(String title, String message) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setType(NotificationType.GROUP_INVITATION_CREATED);
        request.setTitle(title);
        request.setMessage(message);
        request.setResourceType("GROUP");
        request.setResourceId(UUID.randomUUID());
        return request;
    }
}
