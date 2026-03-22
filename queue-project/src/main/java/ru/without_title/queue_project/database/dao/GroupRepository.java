package ru.without_title.queue_project.database.dao;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.without_title.queue_project.database.entities.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {
    // Найти все активные группы
    List<Group> findByIsActiveTrue();

    // Найти группу по ID (только активные)
    Optional<Group> findByGroupIdAndIsActiveTrue(UUID groupId);
}
