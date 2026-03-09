package ru.without_title.queue_project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import java.util.UUID;

public class JoinQueueRequest {

    @NotNull(message = "Queue ID is required")
    private UUID queueId;

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Position is required")
    @Min(value = 1, message = "Position must be at least 1")
    private Integer desiredPosition;

    public UUID getQueueId() { return queueId; }
    public void setQueueId(UUID queueId) { this.queueId = queueId; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public Integer getDesiredPosition() { return desiredPosition; }
    public void setDesiredPosition(Integer desiredPosition) { this.desiredPosition = desiredPosition; }
}
