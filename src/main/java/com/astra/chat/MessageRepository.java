package com.astra.chat;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    @Query("""
            select m from Message m
            where (m.senderId = :a and m.recipientId = :b)
               or (m.senderId = :b and m.recipientId = :a)
            order by m.createdAt desc
            """)
    List<Message> findConversation(@Param("a") UUID a, @Param("b") UUID b, Pageable pageable);

    long countBySenderIdAndRecipientIdAndReadAtIsNull(UUID senderId, UUID recipientId);

    long countByRecipientIdAndReadAtIsNull(UUID recipientId);

    @Modifying
    @Query("""
            update Message m set m.readAt = :now
            where m.senderId = :senderId and m.recipientId = :recipientId and m.readAt is null
            """)
    void markRead(@Param("senderId") UUID senderId, @Param("recipientId") UUID recipientId, @Param("now") OffsetDateTime now);
}
