package ru.without_title.queue_project.dto.request;

import ru.without_title.queue_project.database.entities.enums.GroupRole;

// import java.util.UUID;

public class GroupMemberUpdateRequest {

    private GroupRole role;

    public GroupMemberUpdateRequest() {
    }

    public GroupMemberUpdateRequest(GroupRole role) {
        this.role = role;
    }

    public GroupRole getRole() {
        return role;
    }

    public void setRole(GroupRole role) {
        this.role = role;
    }
}
