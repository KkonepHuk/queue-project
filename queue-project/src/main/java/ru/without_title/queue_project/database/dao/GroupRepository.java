package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.without_title.queue_project.database.entities.Group;

import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

}