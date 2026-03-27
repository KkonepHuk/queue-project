package ru.without_title.queue_project.services;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.without_title.queue_project.database.dao.GroupMemberRepository;
import ru.without_title.queue_project.database.dao.GroupRepository;
import ru.without_title.queue_project.database.dao.UserRepository;
import ru.without_title.queue_project.database.entities.Group;
import ru.without_title.queue_project.database.entities.GroupMember;
import ru.without_title.queue_project.database.entities.User;
import ru.without_title.queue_project.database.entities.enums.GroupRole;
import ru.without_title.queue_project.database.entities.enums.SystemRole;
import ru.without_title.queue_project.dto.request.UserRegistrationRequest;
import ru.without_title.queue_project.dto.response.GroupForCuratorResponse;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PasswordEncoder passwordEncoder;

    private static final int MIN_CURATORS_PER_GROUP = 2;

    public UserService(UserRepository userRepository,
            GroupRepository groupRepository,
            GroupMemberRepository groupMemberRepository) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /**
     * Получить список групп, которым нужны кураторы
     */
    public List<GroupForCuratorResponse> getGroupsNeedingCurators() {
        List<Group> needyGroups = groupMemberRepository.findGroupsWithInsufficientCurators(MIN_CURATORS_PER_GROUP);

        return needyGroups.stream()
                .map(group -> {
                    int curatorCount = groupMemberRepository.findByGroupAndRole(group, GroupRole.CURATOR).size();
                    // TODO: заменить 0 на реальное количество жалоб, когда добавишь таблицу reports
                    return GroupForCuratorResponse.fromEntity(group, curatorCount, 0);
                })
                .collect(Collectors.toList());
    }

    /**
     * Регистрация пользователя
     */
    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        // Проверяем email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Создаем пользователя
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);

        // Регистрируем в зависимости от типа
        if (request.getRegistrationType() == UserRegistrationRequest.RegistrationType.CURATOR) {
            user.setRole(SystemRole.SYSTEM_ADMIN);
            User savedUser = userRepository.save(user);
            assignCuratorToGroups(savedUser, request.getSelectedGroupIds());
            return savedUser;
        } else {
            user.setRole(SystemRole.USER);
            return userRepository.save(user);
        }
    }

    /**
     * Назначить пользователя куратором групп
     */
    private void assignCuratorToGroups(User curator, List<UUID> groupIds) {
        if (groupIds == null || groupIds.size() < 2) {
            throw new RuntimeException("Curator must select at least 2 groups");
        }

        for (UUID groupId : groupIds) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new RuntimeException("Group not found: " + groupId));

            // Проверяем, не является ли уже куратором
            if (groupMemberRepository.existsByGroupAndUserAndRole(group, curator, GroupRole.CURATOR)) {
                throw new RuntimeException("User is already curator of group: " + group.getName());
            }

            // Создаем запись в group_members с ролью CURATOR
            GroupMember groupMember = new GroupMember(group, curator, GroupRole.CURATOR);
            groupMemberRepository.save(groupMember);
        }
    }

    /**
     * Логин пользователя
     */
    public User loginUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        return user;
    }

    /**
     * Получить пользователя по ID
     */
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Получить все группы, которые курирует пользователь
     */
    public List<Group> getCuratorGroups(UUID userId) {
        User user = getUserById(userId);
        return groupMemberRepository.findCuratorGroups(user)
                .stream()
                .map(GroupMember::getGroup)
                .collect(Collectors.toList());
    }
}
