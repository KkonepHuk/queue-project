package ru.without_title.queue_project.services;

import ru.without_title.queue_project.database.entities.Queue;
import ru.without_title.queue_project.database.entities.QueueEntry;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.entities.enums.QueueStatus;
import ru.without_title.queue_project.dto.JoinQueueRequest;
import ru.without_title.queue_project.dto.QueueEntryResponse;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.time.LocalDateTime;
import java.util.UUID;

public class QueueService {

    private final EntityManager entityManager;

    public QueueService() {
        // Создаем EntityManager (подключение к БД)
        this.entityManager = Persistence
                .createEntityManagerFactory("your-persistence-unit")
                .createEntityManager();
    }

    public QueueEntryResponse joinQueue(JoinQueueRequest request) {
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();

            // 1. Находим очередь
            Queue queue = entityManager.find(Queue.class, request.getQueueId());
            if (queue == null || !queue.isActive()) {
                throw new RuntimeException("Queue not found or not active");
            }

            // 2. Проверяем время
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(queue.getRegOpen()) || now.isAfter(queue.getRegClose())) {
                throw new RuntimeException("Registration is not open at this time");
            }

            // 3. Проверяем, не в очереди ли уже пользователь
            Long count = entityManager.createQuery(
                            "SELECT COUNT(qe) FROM QueueEntry qe WHERE qe.queueId = :queueId AND qe.userId = :userId",
                            Long.class)
                    .setParameter("queueId", request.getQueueId())
                    .setParameter("userId", request.getUserId())
                    .getSingleResult();

            if (count > 0) {
                throw new RuntimeException("User is already in this queue");
            }

            // 4. Получаем пользователя
            User user = entityManager.find(User.class, request.getUserId());
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            // 5. Проверяем позицию
            validateDesiredPosition(queue, request.getDesiredPosition());

            // 6. Сдвигаем позиции
            shiftPositionsIfNeeded(queue.getQueueId(), request.getDesiredPosition());

            // 7. Создаем запись
            QueueEntry queueEntry = new QueueEntry();
            queueEntry.setQueueId(queue.getQueueId());
            queueEntry.setUserId(request.getUserId());
            queueEntry.setPosition(request.getDesiredPosition());
            queueEntry.setStatus(QueueStatus.WAITING);

            entityManager.persist(queueEntry);
            transaction.commit();

            return mapToResponse(queueEntry, user);

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Error joining queue: " + e.getMessage(), e);
        }
    }

    private void validateDesiredPosition(Queue queue, Integer desiredPosition) {
        if (desiredPosition > queue.getMaxSize()) {
            throw new RuntimeException("Position cannot exceed queue max size: " + queue.getMaxSize());
        }

        // Проверяем, свободна ли позиция
        Long count = entityManager.createQuery(
                        "SELECT COUNT(qe) FROM QueueEntry qe WHERE qe.queueId = :queueId AND qe.position = :position",
                        Long.class)
                .setParameter("queueId", queue.getQueueId())
                .setParameter("position", desiredPosition)
                .getSingleResult();

        if (count > 0) {
            throw new RuntimeException("Position " + desiredPosition + " is already taken");
        }
    }

    private void shiftPositionsIfNeeded(UUID queueId, Integer newPosition) {
        // Получаем максимальную позицию
        Integer maxPosition = entityManager.createQuery(
                        "SELECT MAX(qe.position) FROM QueueEntry qe WHERE qe.queueId = :queueId",
                        Integer.class)
                .setParameter("queueId", queueId)
                .getSingleResult();

        if (maxPosition != null && newPosition <= maxPosition) {
            // Сдвигаем позиции
            entityManager.createQuery(
                            "UPDATE QueueEntry qe SET qe.position = qe.position + 1 " +
                                    "WHERE qe.queueId = :queueId AND qe.position >= :fromPosition")
                    .setParameter("queueId", queueId)
                    .setParameter("fromPosition", newPosition)
                    .executeUpdate();
        }
    }

    private QueueEntryResponse mapToResponse(QueueEntry entry, User user) {
        QueueEntryResponse response = new QueueEntryResponse();
        response.setQueueEntryId(entry.getQueueEntryId());
        response.setQueueId(entry.getQueueId());
        response.setUserId(entry.getUserId());
        response.setUserName(user.getFirstName() + " " + user.getLastName());
        response.setPosition(entry.getPosition());
        response.setJoinedAt(entry.getJoinedAt());
        response.setStatus(entry.getStatus().name());
        response.setMessage("Successfully joined queue at position " + entry.getPosition());
        return response;
    }
