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
        String queueTitle,
        String groupName,
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
                entity.getQueue() == null ? null : entity.getQueue().getQueueId(),
                entity.getQueue() == null ? null : entity.getQueue().getTitle(),
                (entity.getQueue() == null || entity.getQueue().getGroup() == null) ? null : entity.getQueue().getGroup().getName(),
                entity.getType(),
                entity.getMessage(),
                entity.getScheduledAt(),
                entity.getSentAt(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
