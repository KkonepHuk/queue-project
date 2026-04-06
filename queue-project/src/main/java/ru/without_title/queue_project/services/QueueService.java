package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;
import ru.without_title.queue_project.dto.request.QueueRequest;
import ru.without_title.queue_project.database.entities.Queue;
import ru.without_title.queue_project.database.dao.*;

@Service
public class QueueService {

    private final QueueRepository queueRepository;
    private final GroupRepository groupRepository; // Предполагаем, что он есть
    private final UserRepository userRepository; // Предполагаем, что он есть

    public QueueService(QueueRepository queueRepository, GroupRepository groupRepository,
            UserRepository userRepository) {
        this.queueRepository = queueRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    public List<Queue> getQueuesByGroupId(UUID groupId) {
        return queueRepository.findByGroup_GroupId(groupId);
    }

    public Queue createQueue(UUID groupId, QueueRequest request) {
        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Группа не найдена"));

        // Временно используем заглушку юзера
        var creator = userRepository.findAll().get(0);

        Queue queue = new Queue();
        queue.setGroup(group);
        queue.setCreatedBy(creator);
        queue.setTitle(request.title());
        queue.setDescription(request.description());
        queue.setEventDate(request.eventDate());
        queue.setRegOpen(request.regOpen());
        queue.setRegClose(request.regClose());
        queue.setMaxSize(request.maxSize());
        queue.setIsActive(true);
        queue.setCreatedAt(LocalDateTime.now());

        return queueRepository.save(queue);
    }

    public Queue getQueueById(UUID queueId) {
        return queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Очередь не найдена"));
    }

    public Queue updateQueue(UUID queueId, QueueRequest request) {
        Queue queue = getQueueById(queueId);
        queue.setTitle(request.title());
        queue.setDescription(request.description());
        queue.setEventDate(request.eventDate());
        queue.setRegOpen(request.regOpen());
        queue.setRegClose(request.regClose());
        queue.setMaxSize(request.maxSize());
        return queueRepository.save(queue);
    }

    public void deleteQueue(UUID queueId) {
        queueRepository.deleteById(queueId);
    }
}
