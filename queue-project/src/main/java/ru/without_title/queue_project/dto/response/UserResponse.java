package ru.without_title.queue_project.dto.response;

import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.entities.enums.SystemRole;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private SystemRole role;
    private boolean is_active;
    private LocalDateTime created_at;

    public UserResponse() {
    }

    public UserResponse(UUID userId, String email, String firstName, String lastName, SystemRole role, boolean is_active, LocalDateTime created_at) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.is_active = is_active;
        this.created_at = created_at;
    }

    // ---------------------- Геттеры и сеттеры ----------------------
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public SystemRole getRole() {
        return role;
    }

    public void setRole(SystemRole role) {
        this.role = role;
    }

    public boolean isActive() { return is_active; }

    public void setActive(boolean is_active) {
        this.is_active = is_active;
    }

    public LocalDateTime getCreatedAt() {
        return created_at;
    }

    public void setCreatedAt(LocalDateTime created_at) {
        this.created_at = created_at;
    }

    // ---------------------- Вспомогательный метод ----------------------
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}