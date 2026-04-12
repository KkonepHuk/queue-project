package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
// import ru.without_title.queue_project.database.entities.GroupMember;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.entities.enums.SystemRole;
import ru.without_title.queue_project.database.dao.UserRepository;
import ru.without_title.queue_project.dto.request.UserRegistrationRequest;
import ru.without_title.queue_project.dto.request.UserUpdateRequest;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        user.setPasswordHash(request.getPassword()); // пока plain text
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(SystemRole.USER); // стандартная роль
        user.setActive(true);

        return userRepository.save(user);
    }

    // --------------------- Логин ---------------------
    public User loginUser(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid email or password");
        }

        User user = userOpt.get();

        // Простейшая проверка пароля (plain text)
        if (!user.getPasswordHash().equals(password)) {
            throw new RuntimeException("Invalid email or password");
        }

        return user;
    }

    // --------------------- Получение пользователя по UUID ---------------------
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
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
