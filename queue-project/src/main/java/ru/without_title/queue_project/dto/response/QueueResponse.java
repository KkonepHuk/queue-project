package ru.without_title.queue_project.dto.response;

import java.util.UUID;
import java.time.LocalDateTime;

public record QueueResponse(
        UUID queueId,
        UUID groupId,
        UUID createdBy,
        String title,
        String description,
        LocalDateTime eventDate,
        LocalDateTime regOpen,
        LocalDateTime regClose,
        Integer maxSize,
        boolean isActive,
        LocalDateTime createdAt) {
}
