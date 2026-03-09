package ru.without_title.queue_project.dto.response;

import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.entities.enums.SystemRole;

import java.util.UUID;

public class UserResponse {

    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private SystemRole role;

    public UserResponse() {
    }

    public UserResponse(UUID userId, String email, String firstName, String lastName, SystemRole role) {
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
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

    // ---------------------- Вспомогательный метод ----------------------
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}