package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.without_title.queue_project.database.entities.Notification;
import ru.without_title.queue_project.database.entities.enums.*;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUser_UserIdAndStatus(UUID userId, NotificationStatus status);
}
