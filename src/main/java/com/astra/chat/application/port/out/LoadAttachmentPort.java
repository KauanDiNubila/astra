package com.astra.chat.application.port.out;

import com.astra.chat.domain.ChatImage;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface LoadAttachmentPort {

    Map<UUID, UUID> attachmentIdsFor(Collection<UUID> messageIds);

    Optional<ChatImage> loadFor(UUID messageId);

    Map<UUID, ChatImage> loadForAll(Collection<UUID> messageIds);
}
