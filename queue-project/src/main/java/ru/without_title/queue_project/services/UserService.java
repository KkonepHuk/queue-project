package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import ru.without_title.queue_project.config.SecurityConfig.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.entities.enums.SystemRole;
import ru.without_title.queue_project.database.dao.UserRepository;
import ru.without_title.queue_project.dto.request.UserLoginRequest;
import ru.without_title.queue_project.dto.request.UserRegistrationRequest;
import ru.without_title.queue_project.dto.request.UserUpdateRequest;
import ru.without_title.queue_project.dto.response.UserLoginResponse;
import ru.without_title.queue_project.exceptions.UnauthorizedException;
import ru.without_title.queue_project.security.JwtTokenUtil;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    public final PasswordEncoder passwordEncoder;
    public final JwtTokenUtil jwtTokenUtil;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    // --------------------- Регистрация ---------------------
    public User registerUser(UserRegistrationRequest request) {
        // Проверка уникальности email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        // Создание сущности пользователя
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(SystemRole.USER); // стандартная роль
        user.setActive(true);

        return userRepository.save(user);
    }

    // --------------------- Логин ---------------------
    public UserLoginResponse loginUser(UserLoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtTokenUtil.generateToken(user.getUserId(), user.getEmail(), user.getRole());
        return new UserLoginResponse(token);
    }

    // --------------------- Получение пользователя по UUID ---------------------
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // --------------------- Получение всех пользователей ---------------------
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // --------------------- Обновление данных пользователя ---------------------
    public User updateUser(UUID userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        return userRepository.save(user);
    }

    // --------------------- Деактивация пользователя ---------------------
    public void deactivateUser(UUID userId) {

        User user = getUserById(userId);
        user.setActive(false);
        userRepository.save(user);
    }
}
