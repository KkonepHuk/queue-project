package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.without_title.queue_project.database.entities.Queue;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public interface QueueRepository extends JpaRepository<Queue, UUID> {
    List<Queue> findByGroup_GroupId(UUID groupId);

    Optional<Queue> findByQueueIdAndIsActiveTrue(UUID queueId);
}
