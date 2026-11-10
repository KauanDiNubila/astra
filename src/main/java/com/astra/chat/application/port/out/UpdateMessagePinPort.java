package com.astra.chat.application.port.out;

import com.astra.chat.domain.Message;

public interface UpdateMessagePinPort {

    void updatePin(Message message);
}
