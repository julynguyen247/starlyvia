package org.example.friendservice.repository;

import org.example.friendservice.entity.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {
    List<Friendship> findByUserIdOrFriendId(UUID userId, UUID friendId);

    Optional<Friendship> findByUserIdAndFriendId(UUID userId, UUID friendId);

    @Query("""
            select count(f) > 0
            from Friendship f
            where (f.userId = :firstUserId and f.friendId = :secondUserId)
               or (f.userId = :secondUserId and f.friendId = :firstUserId)
            """)
    boolean existsBetween(
            @Param("firstUserId") UUID firstUserId,
            @Param("secondUserId") UUID secondUserId
    );
}
