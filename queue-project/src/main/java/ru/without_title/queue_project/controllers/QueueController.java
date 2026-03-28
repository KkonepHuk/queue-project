package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.without_title.queue_project.dto.request.QueueRequest;
import ru.without_title.queue_project.dto.response.QueueResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class QueueController {

    // Групповые операции
    @GetMapping("/groups/{groupId}/queues")
    public List<QueueResponse> getQueuesByGroup(@PathVariable UUID groupId) {
        return List.of();
    }

    @PostMapping("/groups/{groupId}/queues")
    @ResponseStatus(HttpStatus.CREATED)
    public QueueResponse createQueue(@PathVariable UUID groupId, @RequestBody QueueRequest request) {
        return null;
    }

    // Операции с конкретной очередью
    @GetMapping("/queues/{queueId}")
    public QueueResponse getQueue(@PathVariable UUID queueId) {
        return null;
    }

    @PatchMapping("/queues/{queueId}")
    public QueueResponse updateQueue(@PathVariable UUID queueId, @RequestBody QueueRequest request) {
        return null;
    }

    @DeleteMapping("/queues/{queueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQueue(@PathVariable UUID queueId) {
    }
}
