package ru.without_title.queue_project.dto.request;

import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

public class UserRegistrationRequest {

    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private String password;

    @NotBlank(message = "Имя обязательно")
    private String firstName;

    @NotBlank(message = "Фамилия обязательна")
    private String lastName;

    @NotNull(message = "Тип регистрации обязателен")
    private RegistrationType registrationType;

    private List<UUID> selectedGroupIds;

    public enum RegistrationType {
        USER,
        CURATOR
    }

    // Конструкторы, геттеры и сеттеры...
    public UserRegistrationRequest() {
    }

    public UserRegistrationRequest(String email, String password, String firstName,
            String lastName,
            RegistrationType registrationType,
            List<UUID> selectedGroupIds) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.registrationType = registrationType;
        this.selectedGroupIds = selectedGroupIds;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public RegistrationType getRegistrationType() {
        return registrationType;
    }

    public void setRegistrationType(RegistrationType registrationType) {
        this.registrationType = registrationType;
    }

    public List<UUID> getSelectedGroupIds() {
        return selectedGroupIds;
    }

    public void setSelectedGroupIds(List<UUID> selectedGroupIds) {
        this.selectedGroupIds = selectedGroupIds;
    }
}
