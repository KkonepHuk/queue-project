package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.without_title.queue_project.dto.request.GroupMemberCreateRequest;
import ru.without_title.queue_project.dto.request.GroupMemberUpdateRequest;
import ru.without_title.queue_project.dto.response.GroupMemberResponse;
import ru.without_title.queue_project.services.GroupMemberService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/members")
public class GroupMemberController {

    private final GroupMemberService service;

    public GroupMemberController(GroupMemberService service) {
        this.service = service;
    }

    // --------------------- Добавить участника ---------------------
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GroupMemberResponse addMember(
            @PathVariable UUID groupId,
            @RequestBody GroupMemberCreateRequest request
    ) {
        return GroupMemberResponse.fromEntity(
                service.addMember(groupId, request)
        );
    }

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupMemberResponse joinGroup(
            @PathVariable UUID groupId,
            Authentication authentication
    ) {
        return GroupMemberResponse.fromEntity(
                service.joinGroup(groupId, authentication.getName())
        );
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveGroup(
            @PathVariable UUID groupId,
            Authentication authentication
    ) {
        service.leaveGroup(groupId, authentication.getName());
    }

    // --------------------- Получить всех ---------------------
    @GetMapping
    public List<GroupMemberResponse> getMembers(@PathVariable UUID groupId) {
        return service.getMembers(groupId)
                .stream()
                .map(GroupMemberResponse::fromEntity)
                .toList();
    }

    // --------------------- Получить одного ---------------------
    @GetMapping("/{memberId}")
    public GroupMemberResponse getMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId
    ) {
        return GroupMemberResponse.fromEntity(
                service.getMember(groupId, memberId)
        );
    }

    // --------------------- Удалить ---------------------
    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMember(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SYSTEM_ADMIN".equals(a.getAuthority()));
        service.removeMember(groupId, memberId, authentication.getName(), isAdmin);
    }

    // --------------------- Изменить роль ---------------------
    @PatchMapping("/{memberId}")
    public GroupMemberResponse updateRole(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @RequestBody GroupMemberUpdateRequest request,
            Authentication authentication
    ) {
        boolean isAdmin = authentication != null && authentication.getAuthorities() != null
                && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SYSTEM_ADMIN".equals(a.getAuthority()));
        return GroupMemberResponse.fromEntity(
                service.updateRole(groupId, memberId, request.getRole(), authentication.getName(), isAdmin)
        );
    }
}
