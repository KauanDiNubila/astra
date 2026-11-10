package com.astra.chat.application.port.out;

import com.astra.chat.domain.Message;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadMessagePort {

    Optional<Message> load(UUID messageId);

    List<Message> loadAll(Collection<UUID> messageIds);
}
