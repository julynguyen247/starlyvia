package org.example.groupservice;

import org.example.groupservice.client.UserClient;
import org.example.groupservice.dto.CreateGroupRequest;
import org.example.groupservice.entity.*;
import org.example.groupservice.event.DomainEventPublisher;
import org.example.groupservice.repository.GroupInvitationRepository;
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
    void cannotInviteMissingUser() {
        UserClient missingUserClient = userId -> false;
        DomainEventPublisher eventPublisher = (topic, key, payload) -> {
        };
        GroupService service = new GroupService(
                planGroupRepository,
                groupMemberRepository,
                groupInvitationRepository,
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
