package com.astra.social.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataFriendshipRepository extends JpaRepository<FriendshipJpaEntity, UUID> {

    @Query("""
            select f from FriendshipJpaEntity f
            where (f.requesterId = :a and f.addresseeId = :b)
               or (f.requesterId = :b and f.addresseeId = :a)
            """)
    Optional<FriendshipJpaEntity> findBetween(@Param("a") UUID a, @Param("b") UUID b);

    @Query("""
            select f from FriendshipJpaEntity f
            where f.status = :status
              and (f.requesterId = :userId or f.addresseeId = :userId)
            """)
    List<FriendshipJpaEntity> findByStatusForUser(@Param("userId") UUID userId, @Param("status") String status);

    @Query("select f from FriendshipJpaEntity f where f.requesterId = :userId or f.addresseeId = :userId")
    List<FriendshipJpaEntity> findAllForUser(@Param("userId") UUID userId);
}
