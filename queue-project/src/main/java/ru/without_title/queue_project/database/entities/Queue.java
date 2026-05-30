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

    @Column(name = "reg_open")
    private LocalDateTime regOpen;

    @Column(name = "reg_close")
    private LocalDateTime regClose;

    @Column(name = "max_size", nullable = false)
    private Integer maxSize;

    @Column(name = "random_queue")
    private boolean randomQueue;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


    public Queue() {
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public UUID getQueueId() {
        return queueId;
    }

    public void setQueueId(UUID queueId) {
        this.queueId = queueId;
    }

    public LocalDateTime getRegClose() {
        return regClose;
    }

    public void setRegClose(LocalDateTime regClose) {
        this.regClose = regClose;
    }

    public LocalDateTime getRegOpen() {
        return regOpen;
    }

    public void setRegOpen(LocalDateTime regOpen) {
        this.regOpen = regOpen;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setRandomQueue(boolean randomQueue) { this.randomQueue = randomQueue; }

    public boolean isRandomQueue() { return randomQueue; }
}
