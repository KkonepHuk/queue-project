package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.without_title.queue_project.database.entities.Notification;
import ru.without_title.queue_project.database.entities.Queue;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.dao.NotificationRepository;
import ru.without_title.queue_project.database.dao.UserRepository;
import ru.without_title.queue_project.database.entities.enums.NotificationStatus;
import ru.without_title.queue_project.database.entities.enums.NotificationType;
import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    public List<Notification> getMyNotifications(String userEmail) {
        UUID currentUserId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();
        return notificationRepository.findByUser_UserIdOrderByCreatedAtDesc(currentUserId);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        UUID currentUserId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();
        notificationRepository.updateAllStatusByUserId(currentUserId, NotificationStatus.READ);
    }

    @Transactional
    public int deleteAllMyNotifications(String userEmail) {
        UUID currentUserId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();
        return notificationRepository.deleteByUser_UserId(currentUserId);
    }

    public void markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }

    public Notification createNotification(User user, Queue queue, NotificationType type, String message) {
        LocalDateTime now = LocalDateTime.now();
        Notification n = new Notification();
        n.setUser(user);
        n.setQueue(queue);
        n.setType(type);
        n.setMessage(message);
        n.setScheduledAt(now);
        n.setSentAt(now);
        n.setStatus(NotificationStatus.SENT);
        return notificationRepository.save(n);
    }
}
