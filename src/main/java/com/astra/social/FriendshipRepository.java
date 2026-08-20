package com.astra.social;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    @Query("""
            select f from Friendship f
            where (f.requesterId = :a and f.addresseeId = :b)
               or (f.requesterId = :b and f.addresseeId = :a)
            """)
    Optional<Friendship> findBetween(@Param("a") UUID a, @Param("b") UUID b);

    List<Friendship> findByAddresseeIdAndStatus(UUID addresseeId, String status);

    List<Friendship> findByRequesterIdAndStatus(UUID requesterId, String status);

    @Query("""
            select f from Friendship f
            where f.status = 'ACCEPTED'
              and (f.requesterId = :userId or f.addresseeId = :userId)
            """)
    List<Friendship> findAcceptedForUser(@Param("userId") UUID userId);
}
