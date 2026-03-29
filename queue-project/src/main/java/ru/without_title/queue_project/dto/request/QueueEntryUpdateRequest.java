package ru.without_title.queue_project.dto.request;

import ru.without_title.queue_project.database.entities.enums.QueueStatus;

public record QueueEntryUpdateRequest(
        QueueStatus status // WAITING, PASSED, SKIPPED
) {
}
