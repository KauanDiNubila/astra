package com.astra.chat.application.port.out;

import com.astra.chat.application.port.in.MessageResponse;
import java.util.List;
import java.util.UUID;

public interface NotifyChatPort {

    void messageSent(MessageResponse message, List<UUID> audience);

    void messageChanged(MessageResponse message, List<UUID> audience);
}
