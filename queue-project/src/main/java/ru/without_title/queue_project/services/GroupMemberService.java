package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.without_title.queue_project.database.dao.GroupMemberRepository;
import ru.without_title.queue_project.database.dao.GroupRepository;
import ru.without_title.queue_project.database.dao.UserRepository;
import ru.without_title.queue_project.database.entities.Group;
import ru.without_title.queue_project.database.entities.GroupMember;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.entities.enums.GroupRole;
import ru.without_title.queue_project.dto.request.GroupMemberCreateRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupMemberService(GroupMemberRepository groupMemberRepository,
            GroupRepository groupRepository,
            UserRepository userRepository) {
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    // --------------------- Добавить участника ---------------------
    public GroupMember addMember(UUID groupId, GroupMemberCreateRequest request) {

        if (groupMemberRepository.existsByGroup_GroupIdAndUser_UserId(
                groupId, request.getUserId())) {
            throw new RuntimeException("User already in group");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(request.getRole());
        member.setJoinedAt(LocalDateTime.now());

        return groupMemberRepository.save(member);
    }

    // --------------------- Получить участника ---------------------
    public GroupMember getMember(UUID groupId, UUID memberId) {

        GroupMember member = groupMemberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("GroupMember not found"));

        if (!member.getGroupId().equals(groupId)) {
            throw new RuntimeException("Member does not belong to this group");
        }

        return member;
    }

    // --------------------- Все участники группы ---------------------
    public List<GroupMember> getMembers(UUID groupId) {
        return groupMemberRepository.findByGroup_GroupId(groupId);
    }

    // --------------------- Удаление ---------------------
    public void removeMember(UUID groupId, UUID memberId) {

        GroupMember member = getMember(groupId, memberId);
        groupMemberRepository.delete(member);
    }

    // --------------------- Изменение роли ---------------------
    // Вспомогательный метод для изменения роли (чуть подправил твой)
    public GroupMember updateRole(UUID groupId, UUID memberId, GroupRole role) {
        // Нельзя просто так взять и назначить OWNER через этот метод,
        // иначе у группы будет два владельца.
        if (role == GroupRole.OWNER) {
            throw new RuntimeException("Use transferOwnership method to change the owner");
        }

        GroupMember member = getMember(groupId, memberId);
        member.setRole(role);
        return groupMemberRepository.save(member);
    }

    @Transactional
    public void transferOwnership(UUID groupId, UUID newOwnerMemberId) {
        // 1. Ищем текущего владельца в этой группе
        GroupMember currentOwner = groupMemberRepository.findByGroup_GroupIdAndRole(groupId, GroupRole.OWNER)
                .orElseThrow(() -> new RuntimeException("Current owner not found. This is a database inconsistency!"));

        // 2. Ищем того, кому передаем права (он уже должен быть в группе)
        GroupMember newOwner = getMember(groupId, newOwnerMemberId);

        // 3. Рокировка: старый становится модератором (или просто участником), новый —
        // овнером
        currentOwner.setRole(GroupRole.MODERATOR);
        newOwner.setRole(GroupRole.OWNER);

        groupMemberRepository.save(currentOwner);
        groupMemberRepository.save(newOwner);
    }

}
