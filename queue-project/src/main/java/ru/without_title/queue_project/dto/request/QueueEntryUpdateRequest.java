package ru.without_title.queue_project.dto.request;

public record QueueEntryUpdateRequest(
        String status // WAITING, PASSED, SKIPPED
) {
}
