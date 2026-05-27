package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.without_title.queue_project.database.entities.Group;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.entities.GroupMember;
import ru.without_title.queue_project.database.dao.GroupRepository;
import ru.without_title.queue_project.database.dao.GroupMemberRepository;
import ru.without_title.queue_project.database.dao.UserRepository;
import ru.without_title.queue_project.dto.request.GroupCreateRequest;
import ru.without_title.queue_project.database.entities.enums.GroupRole;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupService(
            GroupRepository groupRepository,
            UserRepository userRepository,
            GroupMemberRepository groupMemberRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.groupMemberRepository = groupMemberRepository;
    }

    // --------------------- Создание группы ---------------------
    @Transactional // 2. Гарантируем, что либо создастся всё, либо ничего
    public Group createGroup(GroupCreateRequest request) {

        User creator = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Создаем и сохраняем саму группу
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);
        group.setCreatedAt(LocalDateTime.now());

        Group savedGroup = groupRepository.save(group);

        // 3. Сразу добавляем создателя в таблицу участников
        GroupMember ownerMembership = new GroupMember();
        ownerMembership.setGroup(savedGroup);
        ownerMembership.setUser(creator);
        ownerMembership.setRole(GroupRole.OWNER); // Используем твой Enum
        ownerMembership.setJoinedAt(LocalDateTime.now());

        groupMemberRepository.save(ownerMembership);

        return savedGroup;
    }

    // --------------------- Получение группы по UUID ---------------------
    public Group getGroupById(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    // --------------------- Получение всех групп ---------------------
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    public List<Group> getMyGroups(String userEmail) {
        UUID userId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getUserId();

        return groupMemberRepository.findGroupsByUserId(userId);
    }

    // --------------------- Обновление группы ---------------------
    public Group updateGroup(UUID groupId, GroupCreateRequest request) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (request.getName() != null) {
            group.setName(request.getName());
        }

        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }

        /*
         * from this moment we won't change createdBy field
         * if (request.getCreatedBy() != null) {
         * User user = userRepository.findById(request.getCreatedBy())
         * .orElseThrow(() -> new RuntimeException("User not found"));
         * group.setCreatedBy(user);
         * }
         */

        return groupRepository.save(group);
    }

    // --------------------- Удаление группы ---------------------
    @Transactional
    public void deleteGroup(UUID groupId, UUID requesterUserId) {
        // 1. Проверяем, существует ли группа вообще
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        // 2. Ищем, является ли тот, кто делает запрос, участником этой группы
        // Нам нужен именно OWNER для такого серьезного действия
        GroupMember member = groupMemberRepository
                .findByGroup_GroupIdAndUser_UserId(groupId, requesterUserId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));

        // 3. Проверяем роль. Только OWNER имеет право на удаление.
        if (member.getRole() != GroupRole.OWNER) {
            throw new RuntimeException("Only the OWNER can delete the group. Your role: " + member.getRole());
        }

        // 4. Если всё ок — сносим.
        // Благодаря ON DELETE CASCADE в базе, участники и очереди удалятся сами.
        groupRepository.delete(group);
    }
}
