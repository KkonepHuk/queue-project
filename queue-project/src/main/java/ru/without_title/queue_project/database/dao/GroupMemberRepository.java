package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.without_title.queue_project.database.entities.Group;
import ru.without_title.queue_project.database.entities.GroupMember;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.entities.enums.GroupRole;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    // Найти всех участников группы
    List<GroupMember> findByGroup(Group group);

    // Найти всех участников с определенной ролью
    List<GroupMember> findByGroupAndRole(Group group, GroupRole role);

    // Найти все группы, где пользователь является участником
    List<GroupMember> findByUser(User user);

    // Найти все группы, где пользователь является куратором
    @Query("SELECT gm FROM GroupMember gm WHERE gm.user = :user AND gm.role = 'CURATOR'")
    List<GroupMember> findCuratorGroups(@Param("user") User user);

    // Проверить, является ли пользователь куратором группы
    boolean existsByGroupAndUserAndRole(Group group, User user, GroupRole role);

    // Найти группы, у которых недостаточно кураторов
    @Query("SELECT g FROM Group g WHERE " +
            "(SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group = g AND gm.role = 'CURATOR') < :minCurators " +
            "AND EXISTS (SELECT 1 FROM GroupMember gm2 WHERE gm2.group = g)") // группа существует
    List<Group> findGroupsWithInsufficientCurators(@Param("minCurators") int minCurators);
}
