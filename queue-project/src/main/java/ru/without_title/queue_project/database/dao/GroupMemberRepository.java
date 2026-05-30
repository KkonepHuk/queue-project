package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.without_title.queue_project.database.entities.GroupMember;
import ru.without_title.queue_project.database.entities.enums.GroupRole;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    List<GroupMember> findByGroup_GroupId(UUID groupId);

    List<GroupMember> findByUser_UserId(UUID userId);

    boolean existsByGroup_GroupIdAndUser_UserId(UUID groupId, UUID userId);

    Optional<GroupMember> findByGroup_GroupIdAndRole(UUID groupId, GroupRole role);

    Optional<GroupMember> findByGroup_GroupIdAndUser_UserId(UUID groupId, UUID userId);

    int countByGroup_GroupId(UUID groupId);

    @Query("select gm.group from GroupMember gm where gm.user.userId = :userId")
    List<ru.without_title.queue_project.database.entities.Group> findGroupsByUserId(@Param("userId") UUID userId);
}
