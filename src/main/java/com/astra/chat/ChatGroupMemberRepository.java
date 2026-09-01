package com.astra.chat;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatGroupMemberRepository extends JpaRepository<ChatGroupMember, ChatGroupMemberId> {

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);

    List<ChatGroupMember> findByGroupId(UUID groupId);

    List<ChatGroupMember> findByGroupIdIn(Collection<UUID> groupIds);

    List<ChatGroupMember> findByUserId(UUID userId);

    @Query("select m.userId from ChatGroupMember m where m.groupId = :groupId")
    List<UUID> findUserIdsByGroupId(@Param("groupId") UUID groupId);

    @Modifying
    @Query("update ChatGroupMember m set m.lastReadAt = :now where m.groupId = :groupId and m.userId = :userId")
    void markRead(@Param("groupId") UUID groupId, @Param("userId") UUID userId, @Param("now") OffsetDateTime now);

    // Não lidas de cada grupo que "me" participa, numa query só — mesma
    // ideia de MessageRepository.unreadCountsFor, só que comparando contra
    // o last_read_at de cada membership em vez de um read_at por mensagem.
    @Query("""
            select mem.groupId as groupId, count(msg) as unread
            from ChatGroupMember mem
            join Message msg on msg.groupId = mem.groupId
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
