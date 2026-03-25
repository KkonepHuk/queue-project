package ru.without_title.queue_project.dto.response;

import ru.without_title.queue_project.database.entities.Group;
import java.util.UUID;

public class GroupResponse {
    private UUID groupId;
    private String name;
    private String description;

    public GroupResponse() {
    }

    public GroupResponse(UUID groupId, String name, String description) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
    }

    // Тот самый метод, который вызывается в контроллере
    public static GroupResponse fromEntity(Group group) {
        return new GroupResponse(
                group.getGroupId(),
                group.getName(),
                group.getDescription());
    }

    // Геттеры и сеттеры
    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
