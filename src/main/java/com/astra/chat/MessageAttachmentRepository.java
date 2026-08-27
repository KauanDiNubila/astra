package com.astra.chat;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, UUID> {

    Optional<MessageAttachment> findByMessageId(UUID messageId);

    List<MessageAttachment> findByMessageIdIn(Collection<UUID> messageIds);
}
