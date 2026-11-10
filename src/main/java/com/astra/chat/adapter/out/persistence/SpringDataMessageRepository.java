package com.astra.chat.adapter.out.persistence;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataMessageRepository extends JpaRepository<MessageJpaEntity, UUID> {

    List<MessageJpaEntity> findByGroupIdOrderByCreatedAtDesc(UUID groupId, Pageable pageable);

    Optional<MessageJpaEntity> findFirstByGroupIdOrderByCreatedAtDesc(UUID groupId);

    @Query("""
            select m from MessageJpaEntity m
            where least(m.senderId, m.recipientId) = least(:a, :b)
              and greatest(m.senderId, m.recipientId) = greatest(:a, :b)
            order by m.createdAt desc
            """)
    List<MessageJpaEntity> findConversation(@Param("a") UUID a, @Param("b") UUID b, Pageable pageable);

    @Query("""
            select m from MessageJpaEntity m
            where least(m.senderId, m.recipientId) = least(:a, :b)
              and greatest(m.senderId, m.recipientId) = greatest(:a, :b)
              and m.groupId is null
              and m.pinnedAt is not null
            order by m.pinnedAt desc
            """)
    List<MessageJpaEntity> findPinnedInConversation(@Param("a") UUID a, @Param("b") UUID b);

    List<MessageJpaEntity> findByGroupIdAndPinnedAtIsNotNullOrderByPinnedAtDesc(UUID groupId);

    long countByRecipientIdAndReadAtIsNull(UUID recipientId);

    @Modifying
    @Query("""
            update MessageJpaEntity m set m.readAt = :now
            where m.senderId = :senderId and m.recipientId = :recipientId and m.readAt is null
            """)
    void markRead(@Param("senderId") UUID senderId, @Param("recipientId") UUID recipientId,
                  @Param("now") OffsetDateTime now);

    @Query("""
            select m.senderId as friendId, count(m) as unread
            from MessageJpaEntity m
            where m.recipientId = :me and m.senderId in :friendIds and m.readAt is null
            group by m.senderId
            """)
    List<UnreadBySender> unreadCountsFor(@Param("me") UUID me, @Param("friendIds") Collection<UUID> friendIds);

    @Query(value = """
            select distinct on (least(sender_id, recipient_id), greatest(sender_id, recipient_id))
                   sender_id, recipient_id, content, created_at
            from message
            where (sender_id = :me and recipient_id in (:friendIds))
               or (recipient_id = :me and sender_id in (:friendIds))
            order by least(sender_id, recipient_id), greatest(sender_id, recipient_id), created_at desc
            """, nativeQuery = true)
    List<LastMessageView> lastMessagesFor(@Param("me") UUID me, @Param("friendIds") Collection<UUID> friendIds);

    @Query("select m from MessageJpaEntity m where m.senderId = :userId or m.recipientId = :userId order by m.createdAt")
    List<MessageJpaEntity> findAllForExport(@Param("userId") UUID userId);

    interface UnreadBySender {
        UUID getFriendId();

        long getUnread();
    }

    interface LastMessageView {
        UUID getSenderId();

        UUID getRecipientId();

        String getContent();

        Instant getCreatedAt();
    }
}
