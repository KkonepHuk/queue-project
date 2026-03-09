package ru.without_title.queue_project.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class QueueEntryResponse {
    private UUID queueEntryId;
    private UUID queueId;
    private UUID userId;
    private String userName;
    private Integer position;
    private LocalDateTime joinedAt;
    private String status;
    private String message;

    public UUID getQueueEntryId() { return queueEntryId; }
    public void setQueueEntryId(UUID queueEntryId) { this.queueEntryId = queueEntryId; }

    public UUID getQueueId() { return queueId; }
    public void setQueueId(UUID queueId) { this.queueId = queueId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
