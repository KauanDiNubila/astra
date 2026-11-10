package com.astra.chat.application.port.out;

import com.astra.chat.domain.ChatImage;
import com.astra.chat.domain.Message;

public interface CreateMessagePort {

    Message create(Message message, ChatImage attachment);
}
