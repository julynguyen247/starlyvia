package org.example.notificationservice.dto;

public record NotificationRealtimeMessage(
        String type,
        NotificationResponse notification
) {
    public static NotificationRealtimeMessage created(NotificationResponse notification) {
        return new NotificationRealtimeMessage("NOTIFICATION_CREATED", notification);
    }
}
