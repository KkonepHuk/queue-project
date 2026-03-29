package ru.without_title.queue_project.dto.request;

import ru.without_title.queue_project.database.entities.enums.GroupRole;

import java.util.UUID;

public class GroupMemberCreateRequest {

    private UUID userId;
    private GroupRole role;

    public GroupMemberCreateRequest() {
    }

    public GroupMemberCreateRequest(UUID userId, GroupRole role) {
        this.userId = userId;
        this.role = role;
    }

    public UUID getUserId() { return userId; }

    public void setUserId(UUID userId) { this.userId = userId; }

    public GroupRole getRole() { return role; }

    public void setRole(GroupRole role) { this.role = role; }
}