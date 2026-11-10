package com.astra.chat.application.port.out;

import com.astra.chat.domain.ChatGroup;

public interface CreateGroupPort {

    ChatGroup create(ChatGroup group);
}
