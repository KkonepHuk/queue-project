package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import ru.without_title.queue_project.dto.request.QueueRequest;
import ru.without_title.queue_project.database.entities.Queue;

// TODO: ЭТО ЗАГЛУШКА СРАНАЯ МАТЬ ЕГО ЗАГЛУШКА НАДО СДЕЛАТЬ С ЭТИМ ЧТО-ТО
@Service
public class QueueService {
    public List<Queue> getQueuesByGroupId(UUID groupId) {
        return List.of();
    }

    public Queue createQueue(UUID groupId, QueueRequest request) {
        return null;
    }

    public Queue getQueueById(UUID queueId) {
        return null;
    }

    public Queue updateQueue(UUID queueId, QueueRequest request) {
        return null;
    }

    public void deleteQueue(UUID queueId) {
    }
}
