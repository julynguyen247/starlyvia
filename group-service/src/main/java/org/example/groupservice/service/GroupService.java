package org.example.groupservice.service;

import lombok.RequiredArgsConstructor;
import org.example.groupservice.client.UserClient;
import org.example.groupservice.dto.CreateGroupRequest;
import org.example.groupservice.dto.GroupJoinCodeResponse;
import org.example.groupservice.dto.GroupJoinPreviewResponse;
import org.example.groupservice.entity.*;
import org.example.groupservice.event.DomainEventPublisher;
import org.example.groupservice.event.GroupEvent;
import org.example.groupservice.repository.GroupInvitationRepository;
import org.example.groupservice.repository.GroupJoinCodeRepository;
import org.example.groupservice.repository.GroupMemberRepository;
import org.example.groupservice.repository.PlanGroupRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    private static final int GROUP_EVENT_VERSION = 1;
    private static final int JOIN_CODE_VALID_DAYS = 7;

    private final PlanGroupRepository planGroupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final GroupJoinCodeRepository groupJoinCodeRepository;
    private final UserClient userClient;
    private final DomainEventPublisher eventPublisher;

    @Value("${app.kafka.topics.group-events:group.events}")
    private String groupEventsTopic;

    @Transactional
    public PlanGroup create(UUID currentUserId, CreateGroupRequest request) {
        PlanGroup group = PlanGroup.builder()
                .name(request.getName())
                .description(request.getDescription())
                .type(request.getType() == null ? GroupType.CUSTOM : request.getType())
                .createdBy(currentUserId)
                .build();

        PlanGroup savedGroup = planGroupRepository.save(group);
        groupMemberRepository.save(GroupMember.builder()
                .group(savedGroup)
                .userId(currentUserId)
                .role(GroupRole.OWNER)
                .build());
        publishGroupEvent("group.created", savedGroup.getId(), currentUserId, currentUserId);
        return savedGroup;
    }

    @Transactional(readOnly = true)
    public List<PlanGroup> getMyGroups(UUID currentUserId) {
        return planGroupRepository.findByMemberUserId(currentUserId);
    }

    @Transactional(readOnly = true)
    public PlanGroup getById(UUID currentUserId, UUID groupId) {
        assertMember(groupId, currentUserId);
        return findGroup(groupId);
    }

    @Transactional(readOnly = true)
    public List<GroupMember> getMembers(UUID currentUserId, UUID groupId) {
        assertMember(groupId, currentUserId);
        return groupMemberRepository.findByGroupId(groupId);
    }

    @Transactional
    public GroupJoinCodeResponse getOrCreateJoinCode(UUID currentUserId, UUID groupId) {
        PlanGroup group = findGroup(groupId);
        assertAdmin(groupId, currentUserId);
        if (group.getType() == GroupType.SOLO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solo trips cannot accept members");
        }

        LocalDateTime now = LocalDateTime.now();
        GroupJoinCode joinCode = groupJoinCodeRepository.findByGroupId(groupId)
                .orElseGet(() -> GroupJoinCode.builder()
                        .group(group)
                        .createdBy(currentUserId)
                        .build());

        if (joinCode.getToken() == null
                || joinCode.getExpiresAt() == null
                || !joinCode.getExpiresAt().isAfter(now)) {
            joinCode.setToken(UUID.randomUUID());
            joinCode.setCreatedBy(currentUserId);
            joinCode.setExpiresAt(now.plusDays(JOIN_CODE_VALID_DAYS));
            joinCode = groupJoinCodeRepository.save(joinCode);
        }

        return toJoinCodeResponse(joinCode);
    }

    @Transactional(readOnly = true)
    public GroupJoinPreviewResponse previewJoinCode(UUID currentUserId, UUID token) {
        GroupJoinCode joinCode = findActiveJoinCode(token);
        PlanGroup group = joinCode.getGroup();
        return new GroupJoinPreviewResponse(
                group.getId(),
                group.getName(),
                group.getDescription(),
                group.getType(),
                joinCode.getExpiresAt(),
                groupMemberRepository.existsByGroupIdAndUserId(group.getId(), currentUserId)
        );
    }

    @Transactional
    public GroupMember joinByCode(UUID currentUserId, UUID token) {
        GroupJoinCode joinCode = findActiveJoinCode(token);
        PlanGroup group = joinCode.getGroup();
        UUID groupId = group.getId();

        GroupMember existingMember = groupMemberRepository.findByGroupIdAndUserId(groupId, currentUserId)
                .orElse(null);
        if (existingMember != null) {
            return existingMember;
        }

        GroupMember member = groupMemberRepository.save(GroupMember.builder()
                .group(group)
                .userId(currentUserId)
                .role(GroupRole.MEMBER)
                .build());
        groupInvitationRepository.findByGroupIdAndInviteeId(groupId, currentUserId)
                .ifPresent(invitation -> invitation.setStatus(GroupInvitationStatus.ACCEPTED));
        publishGroupEvent("group.member.added", groupId, currentUserId, currentUserId);
        return member;
    }

    @Transactional
    public GroupInvitation invite(UUID currentUserId, UUID groupId, UUID inviteeId) {
        if (currentUserId.equals(inviteeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot invite yourself");
        }
        if (!userClient.exists(inviteeId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invitee user not found");
        }
        PlanGroup group = findGroup(groupId);
        assertAdmin(groupId, currentUserId);
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, inviteeId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a group member");
        }

        GroupInvitation invitation = groupInvitationRepository.findByGroupIdAndInviteeId(groupId, inviteeId)
                .orElseGet(() -> GroupInvitation.builder()
                        .group(group)
                        .inviteeId(inviteeId)
                        .build());
        if (invitation.getStatus() == GroupInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending invitation already exists");
        }
        invitation.setInviterId(currentUserId);
        invitation.setStatus(GroupInvitationStatus.PENDING);

        GroupInvitation savedInvitation = groupInvitationRepository.save(invitation);
        publishGroupEvent("group.invitation.created", groupId, currentUserId, inviteeId);
        return savedInvitation;
    }

    @Transactional(readOnly = true)
    public List<GroupInvitation> incomingInvitations(UUID currentUserId) {
        return groupInvitationRepository.findByInviteeId(currentUserId);
    }

    @Transactional(readOnly = true)
    public List<GroupInvitation> outgoingInvitations(UUID currentUserId) {
        return groupInvitationRepository.findByInviterId(currentUserId);
    }

    @Transactional
    public GroupMember acceptInvitation(UUID currentUserId, UUID invitationId) {
        GroupInvitation invitation = findInvitation(invitationId);
        if (!invitation.getInviteeId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the invitee can accept this invitation");
        }
        if (invitation.getStatus() != GroupInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation is not pending");
        }
        UUID groupId = invitation.getGroup().getId();
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
            invitation.setStatus(GroupInvitationStatus.ACCEPTED);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User is already a group member");
        }

        invitation.setStatus(GroupInvitationStatus.ACCEPTED);
        GroupMember member = groupMemberRepository.save(GroupMember.builder()
                .group(invitation.getGroup())
                .userId(currentUserId)
                .role(GroupRole.MEMBER)
                .build());
        publishGroupEvent("group.member.added", groupId, currentUserId, currentUserId);
        return member;
    }

    @Transactional
    public GroupInvitation rejectInvitation(UUID currentUserId, UUID invitationId) {
        GroupInvitation invitation = findInvitation(invitationId);
        if (!invitation.getInviteeId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the invitee can reject this invitation");
        }
        if (invitation.getStatus() != GroupInvitationStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Invitation is not pending");
        }
        invitation.setStatus(GroupInvitationStatus.REJECTED);
        return groupInvitationRepository.save(invitation);
    }

    @Transactional
    public void removeMember(UUID currentUserId, UUID groupId, UUID memberUserId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, memberUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group member not found"));
        boolean leavingSelf = currentUserId.equals(memberUserId);
        if (!leavingSelf) {
            assertAdmin(groupId, currentUserId);
        }
        if (member.getRole() == GroupRole.OWNER && groupMemberRepository.countByGroupId(groupId) > 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Transfer ownership before removing the owner");
        }

        groupMemberRepository.deleteByGroupIdAndUserId(groupId, memberUserId);
        publishGroupEvent("group.member.removed", groupId, currentUserId, memberUserId);
    }

    private PlanGroup findGroup(UUID groupId) {
        return planGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group not found"));
    }

    private GroupInvitation findInvitation(UUID invitationId) {
        return groupInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Group invitation not found"));
    }

    private GroupJoinCode findActiveJoinCode(UUID token) {
        GroupJoinCode joinCode = groupJoinCodeRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Join code not found"));
        if (!joinCode.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Join code has expired");
        }
        return joinCode;
    }

    private GroupJoinCodeResponse toJoinCodeResponse(GroupJoinCode joinCode) {
        return new GroupJoinCodeResponse(
                joinCode.getToken(),
                joinCode.getGroup().getId(),
                joinCode.getGroup().getName(),
                joinCode.getExpiresAt()
        );
    }

    private void assertMember(UUID groupId, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a group member");
        }
    }

    private void assertAdmin(UUID groupId, UUID userId) {
        GroupMember member = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not a group member"));
        if (member.getRole() != GroupRole.OWNER && member.getRole() != GroupRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only group owners or admins can do this");
        }
    }

    private void publishGroupEvent(String eventType, UUID groupId, UUID actorId, UUID targetUserId) {
        GroupEvent payload = new GroupEvent(
                UUID.randomUUID(),
                eventType,
                GROUP_EVENT_VERSION,
                LocalDateTime.now().toString(),
                groupId,
                actorId,
                targetUserId
        );
        eventPublisher.publish(groupEventsTopic, groupId.toString(), payload);
    }
}
