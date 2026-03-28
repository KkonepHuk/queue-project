package ru.without_title.queue_project.dto.response;

import java.util.UUID;
import java.time.LocalDateTime;

public record QueueEntryResponse(
        UUID queueEntryId,
        Integer position,
        String status, // WAITING, PASSED, SKIPPED
        LocalDateTime joinedAt) {
}
