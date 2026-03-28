package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import ru.without_title.queue_project.database.entities.QueueEntry;
import java.util.List;
import java.util.UUID;

@Service
public class QueueEntryService {

    public void joinQueue(UUID queueId) {
        // TODO: Логика входа в очередь
    }

    public void leaveQueue(UUID queueId) {
        // TODO: Логи Crane выхода из очереди
    }

    public List<QueueEntry> getEntriesByQueueId(UUID queueId) {
        // TODO: Получить список всех участников очереди
        return List.of();
    }

    public QueueEntry updateStatus(UUID queueId, UUID userId, String status) {
        // TODO: Обновить статус участника (WAITING -> PASSED и т.д.)
        return null;
    }
}
