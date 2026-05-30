package ru.without_title.queue_project.dto.response;

import java.util.UUID;
import java.time.LocalDateTime;
import ru.without_title.queue_project.database.entities.Queue;

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
        boolean randomQueue,
        boolean isActive,
        LocalDateTime createdAt) {
    public static QueueResponse fromEntity(Queue entity) {
        return new QueueResponse(
                entity.getQueueId(),
                entity.getGroup().getGroupId(),
                entity.getCreatedBy().getUserId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getEventDate(),
                entity.getRegOpen(),
                entity.getRegClose(),
                entity.getMaxSize(),
                entity.isRandomQueue(),
                entity.getIsActive(),
                entity.getCreatedAt());
    }
}
