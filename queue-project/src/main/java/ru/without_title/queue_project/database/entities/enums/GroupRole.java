package ru.without_title.queue_project.database.entities.enums;

public enum GroupRole {
    OWNER,      // Создатель группы
    MODERATOR,  // Модератор группы
    MEMBER,     // Обычный участник
    CURATOR     // Куратор (получает жалобы, но не участвует в жизни группы)
}
