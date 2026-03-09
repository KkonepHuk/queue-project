package ru.without_title.queue_project.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import ru.without_title.queue_project.database.entities.enums.QueueStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queue_entries", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"queue_id", "user_id"}),
        @UniqueConstraint(columnNames = {"queue_id", "position"})
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

    public QueueEntry() {}
}