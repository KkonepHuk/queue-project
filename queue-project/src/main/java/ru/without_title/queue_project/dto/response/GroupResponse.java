package ru.without_title.queue_project.dto.response;

import ru.without_title.queue_project.database.entities.Group;

import java.time.LocalDateTime;
import java.util.UUID;

public class GroupResponse {

    private UUID groupId;
    private String name;
    private String description;
    private UUID createdBy;
    private LocalDateTime createdAt;

    public GroupResponse(UUID groupId, String name, String description, UUID createdBy, LocalDateTime createdAt) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }


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

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public static GroupResponse fromEntity(Group group) {
        return new GroupResponse(
                group.getGroupId(),
                group.getName(),
                group.getDescription(),
                group.getCreatedBy(),
                group.getCreatedAt()
        );
    }
}