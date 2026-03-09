package ru.without_title.queue_project.database.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "queues")
public class Queue {

    @Id
    @GeneratedValue
    @Column(name = "queue_id")
    private UUID queueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @Column(name = "reg_open", nullable = false)
    private LocalDateTime regOpen;

    @Column(name = "reg_close", nullable = false)
    private LocalDateTime regClose;

    @Column(name = "max_size", nullable = false)
    private Integer maxSize;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Queue() {}
}