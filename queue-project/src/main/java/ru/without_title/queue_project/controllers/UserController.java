package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.without_title.queue_project.dto.request.UserLoginRequest;
import ru.without_title.queue_project.dto.request.UserRegistrationRequest;
import ru.without_title.queue_project.dto.request.UserUpdateRequest;
import ru.without_title.queue_project.dto.response.UserLoginResponse;
import ru.without_title.queue_project.dto.response.UserResponse;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.services.UserService;

import java.util.UUID;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --------------------- Получение всех пользователей ---------------------
    @GetMapping()
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserResponse::fromEntity)
                .toList();
    }

    // --------------------- Регистрация ---------------------
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@RequestBody UserRegistrationRequest request) {
        User savedUser = userService.registerUser(request);
        return UserResponse.fromEntity(savedUser);
    }

    // --------------------- Логин ---------------------
    @PostMapping("/login")
    public UserLoginResponse loginUser(@RequestBody UserLoginRequest request) {
        return userService.loginUser(request);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        return UserResponse.fromEntity(user);
    }

    @PatchMapping("/me")
    public UserResponse updateCurrentUser(
            Authentication authentication,
            @RequestBody UserUpdateRequest request
    ) {
        User user = userService.getUserByEmail(authentication.getName());
        User updatedUser = userService.updateUser(user.getUserId(), request);
        return UserResponse.fromEntity(updatedUser);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateCurrentUser(Authentication authentication) {
        User user = userService.getUserByEmail(authentication.getName());
        userService.deactivateUser(user.getUserId());
    }

    // --------------------- Получение пользователя по UUID ---------------------
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return UserResponse.fromEntity(user);
    }

    // --------------------- Обновление данных пользователя ---------------------
    @PatchMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable UUID userId,
            @RequestBody UserUpdateRequest request
    ) {
        User updatedUser = userService.updateUser(userId, request);
        return UserResponse.fromEntity(updatedUser);
    }

    // --------------------- Деактивация пользователя ---------------------
    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateUser(
            @PathVariable UUID userId
    ) {
        userService.deactivateUser(userId);
    }
}
