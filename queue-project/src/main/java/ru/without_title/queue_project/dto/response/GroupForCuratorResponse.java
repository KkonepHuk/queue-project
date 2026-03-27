package ru.without_title.queue_project.dto.response;

import ru.without_title.queue_project.database.entities.Group;
import java.util.UUID;

public class GroupForCuratorResponse {

    private UUID groupId;
    private String name;
    private String description;
    private int curatorCount;
    private int pendingReportsCount; // количество активных жалоб

    public GroupForCuratorResponse() {
    }

    public GroupForCuratorResponse(UUID groupId, String name, String description,
            int curatorCount, int pendingReportsCount) {
        this.groupId = groupId;
        this.name = name;
        this.description = description;
        this.curatorCount = curatorCount;
        this.pendingReportsCount = pendingReportsCount;
    }

    public static GroupForCuratorResponse fromEntity(Group group, int curatorCount, int pendingReportsCount) {
        return new GroupForCuratorResponse(
                group.getGroupId(),
                group.getName(),
                group.getDescription(),
                curatorCount,
                pendingReportsCount);
    }

    // Геттеры и сеттеры
    public UUID getGroupId() {
        return groupId;
    }

    public void setGroupId(UUID groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCuratorCount() {
        return curatorCount;
    }

    public void setCuratorCount(int curatorCount) {
        this.curatorCount = curatorCount;
    }

    public int getPendingReportsCount() {
        return pendingReportsCount;
    }

    public void setPendingReportsCount(int pendingReportsCount) {
        this.pendingReportsCount = pendingReportsCount;
    }
}
