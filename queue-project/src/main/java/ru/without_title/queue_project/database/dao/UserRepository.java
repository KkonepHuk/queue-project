package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.without_title.queue_project.database.entities.User;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Найти пользователя по email
    Optional<User> findByEmail(String email);

    // Проверить, существует ли email
    boolean existsByEmail(String email);

    List<User> findAll();
}