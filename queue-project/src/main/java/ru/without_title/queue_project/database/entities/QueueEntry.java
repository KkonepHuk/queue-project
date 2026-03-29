package ru.without_title.queue_project.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import ru.without_title.queue_project.database.entities.enums.QueueStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queue_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "queue_id", "user_id" }),
        @UniqueConstraint(columnNames = { "queue_id", "position" })
})
public class QueueEntry {

    @Id
    @GeneratedValue
    @Column(name = "queue_entry_id")
    private UUID queueEntryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "queue_id", nullable = false)
    private Queue queue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer position;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status;

    public QueueEntry() {
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public UUID getQueueEntryId() {
        return queueEntryId;
    }

    public void setQueueEntryId(UUID queueEntryId) {
        this.queueEntryId = queueEntryId;
    }

    public Queue getQueue() {
        return queue;
    }

    public void setQueue(Queue queue) {
        this.queue = queue;
    }

    public QueueStatus getStatus() {
        return status;
    }

    public void setStatus(QueueStatus status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
