package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.without_title.queue_project.database.entities.Group;

import java.util.UUID;
import java.util.List;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    // Найти группы по создателю
    List<Group> findByCreatedBy_UserId(UUID userId);

    // Проверка существования группы по имени
    boolean existsByName(String name);
}