package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.without_title.queue_project.dto.request.UserLoginRequest;
import ru.without_title.queue_project.dto.request.UserRegistrationRequest;
import ru.without_title.queue_project.dto.response.UserLoginResponse;
import ru.without_title.queue_project.dto.response.UserResponse;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.services.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Получить группы, которым нужны кураторы
    @GetMapping("/groups-needing-curators")
    public List<GroupForCuratorResponse> getGroupsNeedingCurators() {
        return userService.getGroupsNeedingCurators();
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
        User user = userService.loginUser(request.getEmail(), request.getPassword());
        return new UserLoginResponse(user.getUserId(), user.getEmail());
    }

    // --------------------- Получение пользователя по UUID ---------------------
    @GetMapping("/{userId}")
    public UserResponse getUserById(@PathVariable UUID userId) {
        User user = userService.getUserById(userId);
        return UserResponse.fromEntity(user);
    }

    // Получить группы, которые курирует пользователь
    @GetMapping("/{userId}/curator-groups")
    public List<GroupResponse> getCuratorGroups(@PathVariable UUID userId) {
        List<Group> groups = userService.getCuratorGroups(userId);
        return groups.stream()
                .map(GroupResponse::fromEntity) // нужно будет создать GroupResponse DTO
                .collect(Collectors.toList());
    }
}
