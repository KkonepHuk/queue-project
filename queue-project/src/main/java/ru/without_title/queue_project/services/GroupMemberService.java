package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.without_title.queue_project.database.dao.GroupMemberRepository;
import ru.without_title.queue_project.database.dao.GroupRepository;
import ru.without_title.queue_project.database.dao.QueueEntryRepository;
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
    private final QueueEntryRepository queueEntryRepository;

    public GroupMemberService(GroupMemberRepository groupMemberRepository,
            GroupRepository groupRepository,
            UserRepository userRepository,
            QueueEntryRepository queueEntryRepository) {
        this.groupMemberRepository = groupMemberRepository;
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.queueEntryRepository = queueEntryRepository;
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

    public GroupMember joinGroup(UUID groupId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMemberCreateRequest request = new GroupMemberCreateRequest(
                user.getUserId(),
                GroupRole.MEMBER
        );

        return addMember(groupId, request);
    }

    @Transactional
    public void leaveGroup(UUID groupId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        GroupMember member = groupMemberRepository
                .findByGroup_GroupIdAndUser_UserId(groupId, user.getUserId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));

        // Leave all queues in this group first and then repack positions in affected queues.
        List<UUID> affectedQueues = queueEntryRepository.findQueueIdsByGroupIdAndUserId(groupId, user.getUserId());
        queueEntryRepository.deleteByGroupIdAndUserId(groupId, user.getUserId());
        repackQueues(affectedQueues);

        if (member.getRole() != GroupRole.OWNER) {
            groupMemberRepository.delete(member);
            return;
        }

        // OWNER leaves: transfer ownership if possible, otherwise delete empty group.
        List<GroupMember> members = groupMemberRepository.findByGroup_GroupId(groupId);
        GroupMember newOwner = members.stream()
                .filter(m -> !m.getUserId().equals(user.getUserId()))
                .findFirst()
                .orElse(null);

        if (newOwner == null) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found"));
            groupRepository.delete(group);
            return;
        }

        newOwner.setRole(GroupRole.OWNER);
        groupMemberRepository.save(newOwner);
        groupMemberRepository.delete(member);
    }

    private void repackQueues(List<UUID> queueIds) {
        if (queueIds == null || queueIds.isEmpty()) return;
        final int OFFSET = 1000000;
        for (UUID qid : queueIds) {
            // Pack positions to 1..N. Two-step: bump out of range then repack.
            queueEntryRepository.bumpAllPositions(qid, OFFSET);
            queueEntryRepository.repackPositions(qid);
        }
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
    @Transactional
    public void removeMember(UUID groupId, UUID memberId, String requesterEmail, boolean requesterIsAdmin) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!requesterIsAdmin) {
            GroupMember requesterMember = groupMemberRepository
                    .findByGroup_GroupIdAndUser_UserId(groupId, requester.getUserId())
                    .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
            if (requesterMember.getRole() != GroupRole.OWNER && requesterMember.getRole() != GroupRole.MODERATOR) {
                throw new RuntimeException("Only the group owner or moderator can remove members");
            }
        }

        GroupMember target = getMember(groupId, memberId);

        // Don't allow removing the OWNER via this endpoint.
        if (target.getRole() == GroupRole.OWNER) {
            throw new RuntimeException("Cannot remove the group owner");
        }

        // Don't allow removing yourself here (use leaveGroup).
        if (target.getUserId().equals(requester.getUserId())) {
            throw new RuntimeException("Use leave group to remove yourself");
        }

        // Remove from all queues in this group first.
        List<UUID> affectedQueues = queueEntryRepository.findQueueIdsByGroupIdAndUserId(groupId, target.getUserId());
        queueEntryRepository.deleteByGroupIdAndUserId(groupId, target.getUserId());
        repackQueues(affectedQueues);
        groupMemberRepository.delete(target);
    }

    // --------------------- Изменение роли ---------------------
    @Transactional
    public GroupMember updateRole(UUID groupId, UUID memberId, GroupRole role, String requesterEmail, boolean requesterIsAdmin) {
        if (role == null) {
            throw new RuntimeException("Role is required");
        }
        // Нельзя просто так взять и назначить OWNER через этот метод,
        // иначе у группы будет два владельца.
        if (role == GroupRole.OWNER) {
            throw new RuntimeException("Use transferOwnership method to change the owner");
        }

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!requesterIsAdmin) {
            GroupMember requesterMember = groupMemberRepository
                    .findByGroup_GroupIdAndUser_UserId(groupId, requester.getUserId())
                    .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
            if (requesterMember.getRole() != GroupRole.OWNER) {
                throw new RuntimeException("Only the group owner can change roles");
            }
        }

        GroupMember member = getMember(groupId, memberId);
        if (member.getRole() == GroupRole.OWNER) {
            throw new RuntimeException("Cannot change the group owner's role");
        }

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
