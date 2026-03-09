package ru.without_title.queue_project.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.without_title.queue_project.database.entities.enums.QueueStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queue_entries",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"queue_id", "user_id"}),
                @UniqueConstraint(columnNames = {"queue_id", "position"})
        })
public class QueueEntry {

    @Id
    @GeneratedValue
    @Column(name = "queue_entry_id")
    private UUID queueEntryId;

    @Column(name = "queue_id", nullable = false)
    private UUID queueId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private Integer position;

    @CreationTimestamp
    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status;

    public QueueEntry() {}

    public UUID getQueueEntryId() { return queueEntryId; }
    public void setQueueEntryId(UUID queueEntryId) { this.queueEntryId = queueEntryId; }

    public UUID getQueueId() { return queueId; }
    public void setQueueId(UUID queueId) { this.queueId = queueId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public QueueStatus getStatus() { return status; }
    public void setStatus(QueueStatus status) { this.status = status; }
}
