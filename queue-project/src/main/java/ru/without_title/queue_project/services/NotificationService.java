package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import ru.without_title.queue_project.database.entities.Notification;
import ru.without_title.queue_project.database.dao.NotificationRepository;
import ru.without_title.queue_project.database.dao.UserRepository;
import ru.without_title.queue_project.database.entities.enums.NotificationStatus;
import java.util.List;
import java.util.UUID;

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

    public void markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        notification.setStatus(NotificationStatus.READ);
        notificationRepository.save(notification);
    }
}
