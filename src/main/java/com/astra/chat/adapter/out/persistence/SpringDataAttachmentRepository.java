package com.astra.chat.adapter.out.persistence;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataAttachmentRepository extends JpaRepository<AttachmentJpaEntity, UUID> {

    Optional<AttachmentJpaEntity> findByMessageId(UUID messageId);

    List<AttachmentJpaEntity> findByMessageIdIn(Collection<UUID> messageIds);

    @Query("select a.messageId as messageId, a.id as id from AttachmentJpaEntity a where a.messageId in :messageIds")
    List<AttachmentIdView> findIdsByMessageIdIn(@Param("messageIds") Collection<UUID> messageIds);

    interface AttachmentIdView {
        UUID getMessageId();

        UUID getId();
    }
}
