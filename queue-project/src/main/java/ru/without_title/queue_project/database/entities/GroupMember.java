package ru.without_title.queue_project.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.without_title.queue_project.database.entities.enums.GroupRole;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "group_members", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "group_id", "user_id" })
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

    public GroupMember() {
    }

    // Конструктор для удобства
    public GroupMember(Group group, User user, GroupRole role) {
        this.group = group;
        this.user = user;
        this.role = role;
    }

    // Геттеры и сеттеры
    public UUID getGroupMemberId() {
        return groupMemberId;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public GroupRole getRole() {
        return role;
    }

    public void setRole(GroupRole role) {
        this.role = role;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }
}
