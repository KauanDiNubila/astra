package com.astra.chat.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.astra.chat.entity.MessageAttachment;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, UUID> {

    Optional<MessageAttachment> findByMessageId(UUID messageId);

    List<MessageAttachment> findByMessageIdIn(Collection<UUID> messageIds);
}
