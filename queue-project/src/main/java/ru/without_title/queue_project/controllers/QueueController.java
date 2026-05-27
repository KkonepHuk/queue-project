package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.without_title.queue_project.dto.request.QueueRequest;
import ru.without_title.queue_project.dto.response.QueueResponse;
import ru.without_title.queue_project.database.entities.Queue;
import ru.without_title.queue_project.services.QueueService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class QueueController {

    private final QueueService queueService;

    public QueueController(QueueService queueService) {
        this.queueService = queueService;
    }

    @GetMapping("/groups/{groupId}/queues")
    public List<QueueResponse> getQueuesByGroup(@PathVariable UUID groupId) {
        return queueService.getQueuesByGroupId(groupId).stream()
                .map(QueueResponse::fromEntity)
                .toList();
    }

    @PostMapping("/groups/{groupId}/queues")
    @ResponseStatus(HttpStatus.CREATED)
    public QueueResponse createQueue(
            @PathVariable UUID groupId,
            @RequestBody QueueRequest request,
            Authentication authentication) {
        Queue queue = queueService.createQueue(groupId, request, authentication.getName());
        return QueueResponse.fromEntity(queue);
    }

    @GetMapping("/queues/{queueId}")
    public QueueResponse getQueue(@PathVariable UUID queueId) {
        return QueueResponse.fromEntity(queueService.getQueueById(queueId));
    }

    @PatchMapping("/queues/{queueId}")
    public QueueResponse updateQueue(@PathVariable UUID queueId, @RequestBody QueueRequest request) {
        Queue updated = queueService.updateQueue(queueId, request);
        return QueueResponse.fromEntity(updated);
    }

    @DeleteMapping("/queues/{queueId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQueue(@PathVariable UUID queueId, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_SYSTEM_ADMIN".equals(a.getAuthority()));
        queueService.deleteQueue(queueId, authentication.getName(), isAdmin);
    }

    @PatchMapping("/queues/{queueId}/close")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void closeQueue(@PathVariable UUID queueId, Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_SYSTEM_ADMIN".equals(a.getAuthority()));
        queueService.closeQueue(queueId, authentication.getName(), isAdmin);
    }
}
