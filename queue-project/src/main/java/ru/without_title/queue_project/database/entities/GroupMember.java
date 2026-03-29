package ru.without_title.queue_project.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.without_title.queue_project.database.entities.enums.GroupRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"group_id", "user_id"})
})
public class GroupMember {

    @Id
    @GeneratedValue
    @Column(name = "group_member_id")
    private UUID groupMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole role;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    public GroupMember() {}

    public UUID getGroupMemberId() { return groupMemberId; }

    public void setGroupMemberId(UUID groupMemberId) { this.groupMemberId = groupMemberId; }

    public UUID getGroupId() { return group.getGroupId(); }

    public void setGroup(Group group) { this.group = group; }

    public UUID getUserId() { return user.getUserId(); }

    public void setUser(User user) { this.user = user; }

    public GroupRole getRole() { return role; }

    public void setRole(GroupRole role) { this.role = role; }

    public LocalDateTime getJoinedAt() { return joinedAt; }

    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}