package ru.without_title.queue_project.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;
import ru.without_title.queue_project.database.entities.Notification;
import ru.without_title.queue_project.database.entities.enums.NotificationStatus;
import ru.without_title.queue_project.database.entities.enums.NotificationType;

public record NotificationResponse(
        UUID notificationId,
        UUID userId,
        UUID queueId,
        NotificationType type,
        String message,
        LocalDateTime scheduledAt,
        LocalDateTime sentAt,
        NotificationStatus status,
        LocalDateTime createdAt) {
    public static NotificationResponse fromEntity(Notification entity) {
        return new NotificationResponse(
                entity.getNotificationId(),
                entity.getUser().getUserId(),
                entity.getQueue().getQueueId(),
                entity.getType(),
                entity.getMessage(),
                entity.getScheduledAt(),
                entity.getSentAt(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
