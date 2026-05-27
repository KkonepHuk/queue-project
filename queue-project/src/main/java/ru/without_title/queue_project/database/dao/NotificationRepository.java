package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.without_title.queue_project.database.entities.Notification;
import ru.without_title.queue_project.database.entities.enums.*;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(UUID userId);

    List<Notification> findByUser_UserIdAndStatus(UUID userId, NotificationStatus status);

    @Modifying
    int deleteByUser_UserId(UUID userId);

    @Modifying
    @Query("update Notification n set n.status = :status where n.user.userId = :userId and n.status <> :status")
    int updateAllStatusByUserId(@Param("userId") UUID userId, @Param("status") NotificationStatus status);
}
