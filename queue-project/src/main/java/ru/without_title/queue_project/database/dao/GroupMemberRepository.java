package ru.without_title.queue_project.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.without_title.queue_project.database.entities.GroupMember;

import java.util.List;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    List<GroupMember> findByGroup_GroupId(UUID groupId);

    List<GroupMember> findByUser_UserId(UUID userId);

    boolean existsByGroup_GroupIdAndUser_UserId(UUID groupId, UUID userId);
}