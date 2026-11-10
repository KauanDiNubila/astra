package com.astra.chat.application.port.in;

public interface CreateGroupUseCase {

    GroupConversationSummary create(CreateGroupCommand command);
}
