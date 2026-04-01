package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.without_title.queue_project.database.entities.QueueEntry;
import ru.without_title.queue_project.database.entities.Queue;
import ru.without_title.queue_project.database.entities.enums.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface QueueEntryRepository extends JpaRepository<QueueEntry, UUID> {
    List<QueueEntry> findByQueue_QueueIdOrderByPosition(UUID queueId);

    Optional<QueueEntry> findByQueue_QueueIdAndUser_UserId(UUID queueId, UUID userId);

    long countByQueue(Queue queue);

    boolean existsByQueueAndUser_UserId(Queue queue, UUID userId);

    List<QueueEntry> findByQueue_QueueIdAndStatus(UUID queueId, QueueStatus status);
}
