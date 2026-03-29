package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import ru.without_title.queue_project.database.entities.QueueEntry;
import ru.without_title.queue_project.database.dao.*;
import ru.without_title.queue_project.database.entities.enums.*;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class QueueEntryService {
    private final QueueEntryRepository entryRepository;
    private final QueueRepository queueRepository;
    // UserRepository тоже понадобится, чтобы найти, кто именно записывается

    public QueueEntryService(QueueEntryRepository entryRepository, QueueRepository queueRepository) {
        this.entryRepository = entryRepository;
        this.queueRepository = queueRepository;
    }

    public void joinQueue(UUID queueId) {
        // 1. Ищем очередь
        var queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Очередь не найдена"));

        // 2. Проверяем, активна ли она и открыта ли регистрация
        LocalDateTime now = LocalDateTime.now();
        if (!queue.getIsActive() || now.isBefore(queue.getRegOpen()) || now.isAfter(queue.getRegClose())) {
            throw new RuntimeException("Регистрация в очередь закрыта или еще не началась");
        }

        // 3. Проверяем лимит мест
        long currentCount = entryRepository.countByQueue(queue);
        if (currentCount >= queue.getMaxSize()) {
            throw new RuntimeException("В очереди больше нет свободных мест");
        }

        // 4. Проверяем, не записан ли уже пользователь (Unique constraint в БД!)
        // В будущем userId достанем из SecurityContext. Пока представим, что он у нас
        // есть.
        UUID currentUserId = UUID.randomUUID(); // ЗАГЛУШКА

        if (entryRepository.existsByQueueAndUser_UserId(queue, currentUserId)) {
            throw new RuntimeException("Вы уже записаны в эту очередь");
        }

        // 5. Создаем новую запись
        QueueEntry entry = new QueueEntry();
        entry.setQueue(queue);
        // entry.setUser(userRepository.getReferenceById(currentUserId));
        entry.setPosition((int) currentCount + 1); // Позиция = кол-во людей + 1
        entry.setStatus(QueueStatus.WAITING);
        entry.setJoinedAt(now);

        entryRepository.save(entry);
    }

    public void leaveQueue(UUID queueId) {
        // Заглушка текущего юзера
        UUID currentUserId = UUID.fromString("...");

        QueueEntry entry = entryRepository.findByQueue_QueueIdAndUser_UserId(queueId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Вы не записаны в эту очередь"));

        entryRepository.delete(entry);
        // TODO: нужно добавить логику "сдвига" позиций остальных участников
    }

    public List<QueueEntry> getEntriesByQueueId(UUID queueId) {
        return entryRepository.findByQueue_QueueIdOrderByPosition(queueId);
    }

    public QueueEntry updateStatus(UUID queueId, UUID userId, QueueStatus status) {
        QueueEntry entry = entryRepository.findByQueue_QueueIdAndUser_UserId(queueId, userId)
                .orElseThrow(() -> new RuntimeException("Запись не найдена"));

        entry.setStatus(status);
        return entryRepository.save(entry);
    }
}
