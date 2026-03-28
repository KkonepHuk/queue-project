package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.without_title.queue_project.dto.request.QueueEntryUpdateRequest;
import ru.without_title.queue_project.dto.response.QueueEntryResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/queues/{queueId}") // Базовый путь для удобства
public class QueueEntryController {

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinQueue(@PathVariable UUID queueId) {
        // Логика добавления текущего пользователя
    }

    @DeleteMapping("/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveQueue(@PathVariable UUID queueId) {
        // Логика выхода
    }

    @GetMapping("/entries")
    public List<QueueEntryResponse> getQueueEntries(@PathVariable UUID queueId) {
        return List.of();
    }

    @PatchMapping("/entries/{userId}")
    public QueueEntryResponse updateEntryStatus(
            @PathVariable UUID queueId,
            @PathVariable UUID userId,
            @RequestBody QueueEntryUpdateRequest request) {
        return null;
    }
}
