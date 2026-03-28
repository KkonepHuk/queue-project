package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.without_title.queue_project.dto.response.NotificationResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @GetMapping
    public List<NotificationResponse> getMyNotifications() {
        // TODO: Получить уведомления для текущего авторизованного пользователя
        return List.of();
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(@PathVariable UUID id) {
        // Пометить как прочитанное
    }
}
