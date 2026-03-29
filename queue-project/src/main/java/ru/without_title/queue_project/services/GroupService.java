package ru.without_title.queue_project.services;

import org.springframework.stereotype.Service;
import ru.without_title.queue_project.database.entities.Group;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.dao.GroupRepository;
import ru.without_title.queue_project.database.dao.UserRepository;
import ru.without_title.queue_project.dto.request.GroupCreateRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    public GroupService(GroupRepository groupRepository,
                        UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
    }

    // --------------------- Создание группы ---------------------
    public Group createGroup(GroupCreateRequest request) {

        User creator = userRepository.findById(request.getCreatedBy())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setCreatedBy(creator);
        group.setCreatedAt(LocalDateTime.now());

        return groupRepository.save(group);
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

        if (request.getCreatedBy() != null) {
            User user = userRepository.findById(request.getCreatedBy())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            group.setCreatedBy(user);
        }

        return groupRepository.save(group);
    }

    // --------------------- Удаление группы ---------------------
    public void deleteGroup(UUID groupId) {

        if (!groupRepository.existsById(groupId)) {
            throw new RuntimeException("Group not found");
        }

        groupRepository.deleteById(groupId);
    }
}