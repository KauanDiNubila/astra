package com.astra.chat;

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

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByGroupIdOrderByCreatedAtDesc(UUID groupId, Pageable pageable);

    Optional<Message> findFirstByGroupIdOrderByCreatedAtDesc(UUID groupId);

    // least/greatest casam com o índice idx_message_conversation — escrito
    // como "OR de igualdades" antes, o que Postgres não conseguia associar
    // ao índice (LEAST/GREATEST), forçando um scan da tabela inteira.
    @Query("""
            select m from Message m
            where least(m.senderId, m.recipientId) = least(:a, :b)
              and greatest(m.senderId, m.recipientId) = greatest(:a, :b)
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

    // Contagem de não lidas por remetente, numa query só (em vez de uma
    // COUNT por amigo) — usada pra montar a lista de conversas.
    @Query("""
            select m.senderId as friendId, count(m) as unread
            from Message m
            where m.recipientId = :me and m.senderId in :friendIds and m.readAt is null
            group by m.senderId
            """)
    List<UnreadBySender> unreadCountsFor(@Param("me") UUID me, @Param("friendIds") Collection<UUID> friendIds);

    // Última mensagem de cada conversa envolvendo "me", numa query só.
    // DISTINCT ON exige nativo; a ordenação por LEAST/GREATEST usa o mesmo
    // índice de findConversation.
    @Query(value = """
            select distinct on (least(sender_id, recipient_id), greatest(sender_id, recipient_id))
                   sender_id, recipient_id, content, created_at
            from message
            where (sender_id = :me and recipient_id in (:friendIds))
               or (recipient_id = :me and sender_id in (:friendIds))
            order by least(sender_id, recipient_id), greatest(sender_id, recipient_id), created_at desc
            """, nativeQuery = true)
    List<LastMessageView> lastMessagesFor(@Param("me") UUID me, @Param("friendIds") Collection<UUID> friendIds);

    interface UnreadBySender {
        UUID getFriendId();

        long getUnread();
    }

    interface LastMessageView {
        UUID getSenderId();

        UUID getRecipientId();

        String getContent();

        // Instant, não OffsetDateTime: projeção de query nativa devolve o
        // timestamptz como Instant e o Spring não converte sozinho pra
        // OffsetDateTime aqui (funciona em @Query JPQL, não em nativeQuery).
        Instant getCreatedAt();
    }
}
