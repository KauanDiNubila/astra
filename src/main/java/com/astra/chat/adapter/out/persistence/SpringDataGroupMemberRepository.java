package com.astra.chat.adapter.out.persistence;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataGroupMemberRepository extends JpaRepository<GroupMemberJpaEntity, GroupMemberJpaEntity.Key> {

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    List<GroupMemberJpaEntity> findByGroupId(UUID groupId);

    List<GroupMemberJpaEntity> findByGroupIdIn(Collection<UUID> groupIds);

    @Query("select m.groupId from GroupMemberJpaEntity m where m.userId = :userId")
    List<UUID> findGroupIdsByUserId(@Param("userId") UUID userId);

    @Query("select m.userId from GroupMemberJpaEntity m where m.groupId = :groupId")
    List<UUID> findUserIdsByGroupId(@Param("groupId") UUID groupId);

    @Modifying
    @Query("update GroupMemberJpaEntity m set m.lastReadAt = :now where m.groupId = :groupId and m.userId = :userId")
    int markRead(@Param("groupId") UUID groupId, @Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    @Query("""
            select mem.groupId as groupId, count(msg) as unread
            from GroupMemberJpaEntity mem
            join MessageJpaEntity msg on msg.groupId = mem.groupId
            where mem.userId = :me
              and msg.createdAt > mem.lastReadAt
              and msg.senderId <> :me
              and mem.groupId in :groupIds
            group by mem.groupId
            """)
    List<UnreadByGroup> unreadCountsFor(@Param("me") UUID me, @Param("groupIds") Collection<UUID> groupIds);

    interface UnreadByGroup {
        UUID getGroupId();

        long getUnread();
    }
}
