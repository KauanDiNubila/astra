package com.astra.chat.application.port.in;

public interface SendGroupMessageUseCase {

    MessageResponse send(SendGroupMessageCommand command);
}
