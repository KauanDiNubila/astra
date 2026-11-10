package com.astra.chat.application.service;

import com.astra.chat.application.port.in.MessageResponse;
import com.astra.chat.application.port.in.SendDirectMessageCommand;
import com.astra.chat.application.port.in.SendDirectMessageUseCase;
import com.astra.chat.application.port.out.CheckFriendshipPort;
import com.astra.chat.application.port.out.CreateMessagePort;
import com.astra.chat.application.port.out.LoadMessagePort;
import com.astra.chat.application.port.out.NotifyChatPort;
import com.astra.chat.domain.ChatImage;
import com.astra.chat.domain.Destination;
import com.astra.chat.domain.Message;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SendDirectMessageService implements SendDirectMessageUseCase {

    private final CheckFriendshipPort checkFriendshipPort;
    private final LoadMessagePort loadMessagePort;
    private final CreateMessagePort createMessagePort;
    private final MessageResponses messageResponses;
    private final NotifyChatPort notifyChatPort;

    SendDirectMessageService(CheckFriendshipPort checkFriendshipPort, LoadMessagePort loadMessagePort,
                             CreateMessagePort createMessagePort, MessageResponses messageResponses,
                             NotifyChatPort notifyChatPort) {
        this.checkFriendshipPort = checkFriendshipPort;
        this.loadMessagePort = loadMessagePort;
        this.createMessagePort = createMessagePort;
        this.messageResponses = messageResponses;
        this.notifyChatPort = notifyChatPort;
    }

    @Override
    @Transactional
    public MessageResponse send(SendDirectMessageCommand command) {
        Destination destination = Destination.direct(command.recipientId());
        Message message;
        ChatImage image = null;
        if (command.image() == null) {
            message = Message.text(command.senderId(), destination, command.content());
            Access.requireFriends(checkFriendshipPort, command.senderId(), command.recipientId());
        } else {
            Access.requireFriends(checkFriendshipPort, command.senderId(), command.recipientId());
            image = ChatImage.of(command.image().contentType(), command.image().bytes());
            message = Message.photo(command.senderId(), destination, command.content());
        }
        Replies.attach(loadMessagePort, message, command.replyToMessageId());
        MessageResponse response = messageResponses.one(createMessagePort.create(message, image));
        notifyChatPort.messageSent(response, List.of(command.senderId(), command.recipientId()));
        return response;
    }
}
