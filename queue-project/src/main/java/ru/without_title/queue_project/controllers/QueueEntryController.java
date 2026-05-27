package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.without_title.queue_project.dto.request.QueueEntryUpdateRequest;
import ru.without_title.queue_project.dto.response.QueueEntryResponse;
import ru.without_title.queue_project.database.entities.QueueEntry;
import ru.without_title.queue_project.services.QueueEntryService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/queues/{queueId}")
public class QueueEntryController {

    private final QueueEntryService entryService;

    public QueueEntryController(QueueEntryService entryService) {
        this.entryService = entryService;
    }

    @PostMapping("/join")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void joinQueue(@PathVariable UUID queueId, Authentication authentication) {
        entryService.joinQueue(queueId, authentication.getName());
    }

    @DeleteMapping("/leave")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveQueue(@PathVariable UUID queueId, Authentication authentication) {
        entryService.leaveQueue(queueId, authentication.getName());
    }

    @GetMapping("/entries")
    public List<QueueEntryResponse> getQueueEntries(@PathVariable UUID queueId) {
        return entryService.getEntriesByQueueId(queueId).stream()
                .map(QueueEntryResponse::fromEntity)
                .toList();
    }

    @PatchMapping("/entries/{userId}")
    public QueueEntryResponse updateEntryStatus(
            @PathVariable UUID queueId,
            @PathVariable UUID userId,
            @RequestBody QueueEntryUpdateRequest request,
            Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        boolean isAdmin = authentication.getAuthorities() != null
                && authentication.getAuthorities().stream().anyMatch(a -> "ROLE_SYSTEM_ADMIN".equals(a.getAuthority()));
        QueueEntry updated = entryService.updateStatus(queueId, userId, request.status(), authentication.getName(), isAdmin);
        return QueueEntryResponse.fromEntity(updated);
    }
}
