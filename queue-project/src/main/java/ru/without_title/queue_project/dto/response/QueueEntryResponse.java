package ru.without_title.queue_project.dto.response;

import java.util.UUID;
import java.time.LocalDateTime;
import ru.without_title.queue_project.database.entities.*;
import ru.without_title.queue_project.database.entities.enums.QueueStatus;

public record QueueEntryResponse(
        UUID queueEntryId,
        UUID queueId,
        UUID userId,
        Integer position,
        QueueStatus status, // WAITING, PASSED, SKIPPED
        LocalDateTime joinedAt) {
    public static QueueEntryResponse fromEntity(QueueEntry entity) {
        return new QueueEntryResponse(
                entity.getQueueEntryId(),
                entity.getQueue().getQueueId(),
                entity.getUser().getUserId(),
                entity.getPosition(),
                entity.getStatus(),
                entity.getJoinedAt());
    }
}
