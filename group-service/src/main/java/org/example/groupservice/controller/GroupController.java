package org.example.groupservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.groupservice.dto.CreateGroupRequest;
import org.example.groupservice.dto.GroupJoinCodeResponse;
import org.example.groupservice.dto.GroupJoinPreviewResponse;
import org.example.groupservice.dto.InviteGroupMemberRequest;
import org.example.groupservice.entity.GroupInvitation;
import org.example.groupservice.entity.GroupMember;
import org.example.groupservice.entity.PlanGroup;
import org.example.groupservice.service.GroupService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlanGroup create(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @Valid @RequestBody CreateGroupRequest request
    ) {
        return groupService.create(currentUserId, request);
    }

    @GetMapping
    public List<PlanGroup> getMyGroups(@RequestHeader("X-User-Id") UUID currentUserId) {
        return groupService.getMyGroups(currentUserId);
    }

    @GetMapping("/{groupId}")
    public PlanGroup getById(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID groupId
    ) {
        return groupService.getById(currentUserId, groupId);
    }

    @GetMapping("/{groupId}/members")
    public List<GroupMember> getMembers(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID groupId
    ) {
        return groupService.getMembers(currentUserId, groupId);
    }

    @PostMapping("/{groupId}/join-code")
    public GroupJoinCodeResponse getOrCreateJoinCode(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID groupId
    ) {
        return groupService.getOrCreateJoinCode(currentUserId, groupId);
    }

    @GetMapping("/join-code/{token}")
    public GroupJoinPreviewResponse previewJoinCode(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID token
    ) {
        return groupService.previewJoinCode(currentUserId, token);
    }

    @PostMapping("/join-code/{token}/accept")
    public GroupMember joinByCode(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID token
    ) {
        return groupService.joinByCode(currentUserId, token);
    }

    @PostMapping("/{groupId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupInvitation invite(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID groupId,
            @Valid @RequestBody InviteGroupMemberRequest request
    ) {
        return groupService.invite(currentUserId, groupId, request.getInviteeId());
    }

    @GetMapping("/invitations/incoming")
    public List<GroupInvitation> incomingInvitations(@RequestHeader("X-User-Id") UUID currentUserId) {
        return groupService.incomingInvitations(currentUserId);
    }

    @GetMapping("/invitations/outgoing")
    public List<GroupInvitation> outgoingInvitations(@RequestHeader("X-User-Id") UUID currentUserId) {
        return groupService.outgoingInvitations(currentUserId);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public GroupMember acceptInvitation(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID invitationId
    ) {
        return groupService.acceptInvitation(currentUserId, invitationId);
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public GroupInvitation rejectInvitation(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID invitationId
    ) {
        return groupService.rejectInvitation(currentUserId, invitationId);
    }

    @DeleteMapping("/{groupId}/members/{memberUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(
            @RequestHeader("X-User-Id") UUID currentUserId,
            @PathVariable UUID groupId,
            @PathVariable UUID memberUserId
    ) {
        groupService.removeMember(currentUserId, groupId, memberUserId);
    }
}
