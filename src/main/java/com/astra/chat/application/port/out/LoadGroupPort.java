package com.astra.chat.application.port.out;

import com.astra.chat.domain.ChatGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadGroupPort {

    Optional<ChatGroup> load(UUID groupId);

    List<ChatGroup> listForMember(UUID userId);
}
