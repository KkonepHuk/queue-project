package ru.without_title.queue_project.controllers;

import org.springframework.http.HttpStatus;
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
            @PathVariable UUID memberId
    ) {
        service.removeMember(groupId, memberId);
    }

    // --------------------- Изменить роль ---------------------
    @PatchMapping("/{memberId}")
    public GroupMemberResponse updateRole(
            @PathVariable UUID groupId,
            @PathVariable UUID memberId,
            @RequestBody GroupMemberUpdateRequest request
    ) {
        return GroupMemberResponse.fromEntity(
                service.updateRole(groupId, memberId, request.getRole())
        );
    }
}