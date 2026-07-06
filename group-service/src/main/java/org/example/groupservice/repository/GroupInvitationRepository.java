package org.example.groupservice.repository;

import org.example.groupservice.entity.GroupInvitation;
import org.example.groupservice.entity.GroupInvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, UUID> {
    List<GroupInvitation> findByInviteeId(UUID inviteeId);

    List<GroupInvitation> findByInviterId(UUID inviterId);

    Optional<GroupInvitation> findByGroupIdAndInviteeId(UUID groupId, UUID inviteeId);

    boolean existsByGroupIdAndInviteeIdAndStatus(UUID groupId, UUID inviteeId, GroupInvitationStatus status);
}
