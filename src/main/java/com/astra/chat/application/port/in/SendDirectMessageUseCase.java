package com.astra.chat.application.port.in;

public interface SendDirectMessageUseCase {

    MessageResponse send(SendDirectMessageCommand command);
}
