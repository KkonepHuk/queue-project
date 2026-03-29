package ru.without_title.queue_project.dto.response;

import ru.without_title.queue_project.database.entities.Group;
import ru.without_title.queue_project.database.entities.GroupMember;
import ru.without_title.queue_project.database.entities.enums.GroupRole;

import java.time.LocalDateTime;
import java.util.UUID;

public class GroupMemberResponse {
    private UUID groupMemberId;
    private UUID groupId;
    private UUID userId;
    private GroupRole role;
    private LocalDateTime joinedAt;

    public GroupMemberResponse(UUID groupMemberId, UUID groupId, UUID userId, GroupRole role, LocalDateTime joinedAt) {
        this.groupMemberId = groupMemberId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = joinedAt;
    }


    public UUID getGroupMemberId() { return groupMemberId; }

    public void setGroupMemberId(UUID groupId) { this.groupMemberId = groupMemberId; }

    public UUID getUserId() { return userId; }

    public void setUserId(UUID userId) { this.userId = userId; }

    public GroupRole getRole() { return role; }

    public void setRole(GroupRole role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }

    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }


    public static GroupMemberResponse fromEntity(GroupMember groupMember) {
        return new GroupMemberResponse(
                groupMember.getGroupMemberId(),
                groupMember.getGroupId(),
                groupMember.getUserId(),
                groupMember.getRole(),
                groupMember.getJoinedAt()
        );
    }
}