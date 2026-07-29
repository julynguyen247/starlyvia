package org.example.groupservice;

import org.example.groupservice.client.UserClient;
import org.example.groupservice.dto.CreateGroupRequest;
import org.example.groupservice.dto.GroupJoinCodeResponse;
import org.example.groupservice.dto.GroupJoinPreviewResponse;
import org.example.groupservice.entity.*;
import org.example.groupservice.event.DomainEventPublisher;
import org.example.groupservice.repository.GroupInvitationRepository;
import org.example.groupservice.repository.GroupJoinCodeRepository;
import org.example.groupservice.repository.GroupMemberRepository;
import org.example.groupservice.repository.PlanGroupRepository;
import org.example.groupservice.service.GroupService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class GroupServiceApplicationTests {

    @Autowired
    private GroupService groupService;

    @Autowired
    private PlanGroupRepository planGroupRepository;

    @Autowired
    private GroupMemberRepository groupMemberRepository;

    @Autowired
    private GroupInvitationRepository groupInvitationRepository;

    @Autowired
    private GroupJoinCodeRepository groupJoinCodeRepository;

    @TestConfiguration
    static class UserClientTestConfig {
        @Bean
        @Primary
        UserClient userClient() {
            return userId -> true;
        }

        @Bean
        @Primary
        DomainEventPublisher domainEventPublisher() {
            return (topic, key, payload) -> {
            };
        }
    }

    @BeforeEach
    void cleanDatabase() {
        groupJoinCodeRepository.deleteAll();
        groupInvitationRepository.deleteAll();
        groupMemberRepository.deleteAll();
        planGroupRepository.deleteAll();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void createGroupAddsCreatorAsOwner() {
        UUID ownerId = UUID.randomUUID();

        PlanGroup group = groupService.create(ownerId, createGroupRequest("Weekend Plan", GroupType.FRIENDS));

        GroupMember owner = groupMemberRepository.findByGroupIdAndUserId(group.getId(), ownerId).orElseThrow();
        assertThat(group.getCreatedBy()).isEqualTo(ownerId);
        assertThat(owner.getRole()).isEqualTo(GroupRole.OWNER);
    }

    @Test
    void inviteAndAcceptAddsMember() {
        UUID ownerId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();
        PlanGroup group = groupService.create(ownerId, createGroupRequest("Family Trip", GroupType.FAMILY));

        GroupInvitation invitation = groupService.invite(ownerId, group.getId(), inviteeId);
        GroupMember member = groupService.acceptInvitation(inviteeId, invitation.getId());
        GroupInvitation acceptedInvitation = groupInvitationRepository.findById(invitation.getId()).orElseThrow();

        assertThat(acceptedInvitation.getStatus()).isEqualTo(GroupInvitationStatus.ACCEPTED);
        assertThat(member.getUserId()).isEqualTo(inviteeId);
        assertThat(member.getRole()).isEqualTo(GroupRole.MEMBER);
        assertThat(groupMemberRepository.existsByGroupIdAndUserId(group.getId(), inviteeId)).isTrue();
    }

    @Test
    void userCanBelongToMultipleGroups() {
        UUID ownerId = UUID.randomUUID();

        PlanGroup friends = groupService.create(ownerId, createGroupRequest("Friends", GroupType.FRIENDS));
        PlanGroup family = groupService.create(ownerId, createGroupRequest("Family", GroupType.FAMILY));

        assertThat(groupService.getMyGroups(ownerId))
                .extracting(PlanGroup::getId)
                .containsExactly(family.getId(), friends.getId());
    }

    @Test
    void nonAdminCannotInviteMembers() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();
        PlanGroup group = groupService.create(ownerId, createGroupRequest("Dinner", GroupType.CUSTOM));
        GroupInvitation invitation = groupService.invite(ownerId, group.getId(), memberId);
        groupService.acceptInvitation(memberId, invitation.getId());

        assertThatThrownBy(() -> groupService.invite(memberId, group.getId(), inviteeId))
                .hasMessageContaining("Only group owners or admins");
    }

    @Test
    void ownerCanCreateJoinCodeAndTravelerCanJoin() {
        UUID ownerId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        PlanGroup group = groupService.create(ownerId, createGroupRequest("Beach Trip", GroupType.FRIENDS));

        GroupJoinCodeResponse joinCode = groupService.getOrCreateJoinCode(ownerId, group.getId());
        GroupJoinPreviewResponse preview = groupService.previewJoinCode(travelerId, joinCode.token());
        GroupMember member = groupService.joinByCode(travelerId, joinCode.token());

        assertThat(preview.groupId()).isEqualTo(group.getId());
        assertThat(preview.groupName()).isEqualTo("Beach Trip");
        assertThat(preview.alreadyMember()).isFalse();
        assertThat(member.getUserId()).isEqualTo(travelerId);
        assertThat(member.getRole()).isEqualTo(GroupRole.MEMBER);
    }

    @Test
    void repeatedJoinCodeAndJoinAreIdempotent() {
        UUID ownerId = UUID.randomUUID();
        UUID travelerId = UUID.randomUUID();
        PlanGroup group = groupService.create(ownerId, createGroupRequest("City Trip", GroupType.FAMILY));

        GroupJoinCodeResponse firstCode = groupService.getOrCreateJoinCode(ownerId, group.getId());
        GroupJoinCodeResponse secondCode = groupService.getOrCreateJoinCode(ownerId, group.getId());
        GroupMember firstJoin = groupService.joinByCode(travelerId, firstCode.token());
        GroupMember secondJoin = groupService.joinByCode(travelerId, firstCode.token());

        assertThat(secondCode.token()).isEqualTo(firstCode.token());
        assertThat(secondJoin.getId()).isEqualTo(firstJoin.getId());
        assertThat(groupMemberRepository.countByGroupId(group.getId())).isEqualTo(2);
    }

    @Test
    void expiredJoinCodeCannotBeUsed() {
        UUID ownerId = UUID.randomUUID();
        PlanGroup group = groupService.create(ownerId, createGroupRequest("Old Trip", GroupType.CUSTOM));
        GroupJoinCodeResponse response = groupService.getOrCreateJoinCode(ownerId, group.getId());
        GroupJoinCode joinCode = groupJoinCodeRepository.findByToken(response.token()).orElseThrow();
        joinCode.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        groupJoinCodeRepository.save(joinCode);

        assertThatThrownBy(() -> groupService.previewJoinCode(UUID.randomUUID(), response.token()))
                .hasMessageContaining("Join code has expired");
    }

    @Test
    void regularMemberCannotCreateJoinCode() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        PlanGroup group = groupService.create(ownerId, createGroupRequest("Private Trip", GroupType.FRIENDS));
        GroupInvitation invitation = groupService.invite(ownerId, group.getId(), memberId);
        groupService.acceptInvitation(memberId, invitation.getId());

        assertThatThrownBy(() -> groupService.getOrCreateJoinCode(memberId, group.getId()))
                .hasMessageContaining("Only group owners or admins");
    }

    @Test
    void soloTripCannotCreateJoinCode() {
        UUID ownerId = UUID.randomUUID();
        PlanGroup group = groupService.create(ownerId, createGroupRequest("Solo Trip", GroupType.SOLO));

        assertThatThrownBy(() -> groupService.getOrCreateJoinCode(ownerId, group.getId()))
                .hasMessageContaining("Solo trips cannot accept members");
    }

    @Test
    void ownerCanDeleteGroupAndOwnedRecords() {
        UUID ownerId = UUID.randomUUID();
        UUID inviteeId = UUID.randomUUID();
        PlanGroup group = groupService.create(ownerId, createGroupRequest("Last Adventure", GroupType.FRIENDS));
        groupService.invite(ownerId, group.getId(), inviteeId);
        GroupJoinCodeResponse joinCode = groupService.getOrCreateJoinCode(ownerId, group.getId());

        groupService.delete(ownerId, group.getId());

        assertThat(planGroupRepository.findById(group.getId())).isEmpty();
        assertThat(groupMemberRepository.findByGroupId(group.getId())).isEmpty();
        assertThat(groupInvitationRepository.findByGroupIdAndInviteeId(group.getId(), inviteeId)).isEmpty();
        assertThat(groupJoinCodeRepository.findByToken(joinCode.token())).isEmpty();
    }

    @Test
    void nonOwnerCannotDeleteGroup() {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        PlanGroup group = groupService.create(ownerId, createGroupRequest("Shared Adventure", GroupType.FRIENDS));
        GroupInvitation invitation = groupService.invite(ownerId, group.getId(), memberId);
        groupService.acceptInvitation(memberId, invitation.getId());

        assertThatThrownBy(() -> groupService.delete(memberId, group.getId()))
                .hasMessageContaining("Only the group owner can delete this group");
        assertThat(planGroupRepository.findById(group.getId())).isPresent();
    }

    @Test
    void cannotInviteMissingUser() {
        UserClient missingUserClient = userId -> false;
        DomainEventPublisher eventPublisher = (topic, key, payload) -> {
        };
        GroupService service = new GroupService(
                planGroupRepository,
                groupMemberRepository,
                groupInvitationRepository,
                groupJoinCodeRepository,
                missingUserClient,
                eventPublisher
        );
        UUID ownerId = UUID.randomUUID();
        PlanGroup group = service.create(ownerId, createGroupRequest("Dinner", GroupType.CUSTOM));

        assertThatThrownBy(() -> service.invite(ownerId, group.getId(), UUID.randomUUID()))
                .hasMessageContaining("Invitee user not found");
    }

    private CreateGroupRequest createGroupRequest(String name, GroupType type) {
        CreateGroupRequest request = new CreateGroupRequest();
        request.setName(name);
        request.setType(type);
        return request;
    }
}
