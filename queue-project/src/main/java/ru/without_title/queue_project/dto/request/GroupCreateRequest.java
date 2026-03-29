package ru.without_title.queue_project.dto.request;

import java.util.UUID;

public class GroupCreateRequest {
    private String name;
    private String description;
    private UUID createdBy;

    public GroupCreateRequest() {
    }

    public GroupCreateRequest(String name, String description, UUID createdBy) {
        this.name = name;
        this.description = description;
        this.createdBy = createdBy;
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

}
