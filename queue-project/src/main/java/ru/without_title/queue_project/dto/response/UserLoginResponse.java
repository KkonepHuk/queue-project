package ru.without_title.queue_project.dto.response;

import java.util.UUID;

public class UserLoginResponse {

    private UUID userId;
    private String email;

    public UserLoginResponse() {
    }

    public UserLoginResponse(UUID userId, String email) {
        this.userId = userId;
        this.email = email;
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
}