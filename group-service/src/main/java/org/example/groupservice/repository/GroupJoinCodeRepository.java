package org.example.groupservice.repository;

import org.example.groupservice.entity.GroupJoinCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GroupJoinCodeRepository extends JpaRepository<GroupJoinCode, UUID> {
    Optional<GroupJoinCode> findByGroupId(UUID groupId);

    Optional<GroupJoinCode> findByToken(UUID token);
}
