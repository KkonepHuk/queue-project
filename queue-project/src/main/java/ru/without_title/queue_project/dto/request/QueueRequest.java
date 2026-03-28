package ru.without_title.queue_project.dto.request;

import java.time.LocalDateTime;

public record QueueRequest(
        String title,
        String description,
        LocalDateTime eventDate,
        LocalDateTime regOpen,
        LocalDateTime regClose,
        Integer maxSize) {
}
