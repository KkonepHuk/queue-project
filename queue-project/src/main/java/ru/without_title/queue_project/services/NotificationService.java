package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import ru.without_title.queue_project.database.entities.Notification;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    public List<Notification> getAllMyNotifications() {
        // TODO: Реализовать получение уведомлений текущего пользователя
        return List.of();
    }

    public void markAsRead(UUID id) {
        // TODO: Пометить уведомление как прочитанное
    }
}
