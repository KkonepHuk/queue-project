package ru.without_title.queue_project.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID notificationId,
        UUID userId,
        UUID queueId,
        String type,
        String message,
        LocalDateTime scheduledAt,
        LocalDateTime sentAt,
        String status,
        LocalDateTime createdAt) {
}
