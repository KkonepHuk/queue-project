package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.without_title.queue_project.database.entities.Group;
import ru.without_title.queue_project.dto.request.GroupCreateRequest;
import ru.without_title.queue_project.dto.response.GroupResponse;
import ru.without_title.queue_project.services.GroupService;
import ru.without_title.queue_project.services.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;
    private final UserService userService;

    public GroupController(GroupService groupService, UserService userService) {
        this.groupService = groupService;
        this.userService = userService;
    }

    // --------------------- Создание группы ---------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupResponse createGroup(@RequestBody GroupCreateRequest request, Authentication authentication) {
        request.setCreatedBy(userService.getUserByEmail(authentication.getName()).getUserId());
        Group group = groupService.createGroup(request);
        return GroupResponse.fromEntity(group);
    }

    // --------------------- Получение группы по ID ---------------------
    @GetMapping("/{groupId}")
    public GroupResponse getGroupById(@PathVariable UUID groupId) {
        Group group = groupService.getGroupById(groupId);
        return GroupResponse.fromEntity(group);
    }

    // --------------------- Получение всех групп ---------------------
    @GetMapping
    public List<GroupResponse> getMyGroups(Authentication authentication) {
        return groupService.getMyGroups(authentication.getName())
                .stream()
                .map(GroupResponse::fromEntity)
                .toList();
    }

    // --------------------- Browse: все группы ---------------------
    @GetMapping("/browse")
    public List<GroupResponse> browseGroups() {
        return groupService.getAllGroups()
                .stream()
                .map(GroupResponse::fromEntity)
                .toList();
    }

    // --------------------- Обновление группы ---------------------
    @RequestMapping(value = "/{groupId}", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public GroupResponse updateGroup(
            @PathVariable UUID groupId,
            @RequestBody GroupCreateRequest request) {
        Group group = groupService.updateGroup(groupId, request);
        return GroupResponse.fromEntity(group);
    }

    // --------------------- Удаление группы ---------------------
    @DeleteMapping("/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGroup(@PathVariable UUID groupId, @RequestParam UUID userId) {
        // В будущем userId будет браться автоматически из заголовка/токена
        groupService.deleteGroup(groupId, userId);
    }
}
