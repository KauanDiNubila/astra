package com.astra.chat.application.service;

import com.astra.chat.application.port.in.MessageResponse;
import com.astra.chat.application.port.in.SendGroupMessageCommand;
import com.astra.chat.application.port.in.SendGroupMessageUseCase;
import com.astra.chat.application.port.out.CheckGroupMemberPort;
import com.astra.chat.application.port.out.CreateMessagePort;
import com.astra.chat.application.port.out.LoadMessagePort;
import com.astra.chat.application.port.out.NotifyChatPort;
import com.astra.chat.domain.ChatImage;
import com.astra.chat.domain.Destination;
import com.astra.chat.domain.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SendGroupMessageService implements SendGroupMessageUseCase {

    private final CheckGroupMemberPort checkGroupMemberPort;
    private final LoadMessagePort loadMessagePort;
    private final CreateMessagePort createMessagePort;
    private final MessageResponses messageResponses;
    private final NotifyChatPort notifyChatPort;

    SendGroupMessageService(CheckGroupMemberPort checkGroupMemberPort, LoadMessagePort loadMessagePort,
                            CreateMessagePort createMessagePort, MessageResponses messageResponses,
                            NotifyChatPort notifyChatPort) {
        this.checkGroupMemberPort = checkGroupMemberPort;
        this.loadMessagePort = loadMessagePort;
        this.createMessagePort = createMessagePort;
        this.messageResponses = messageResponses;
        this.notifyChatPort = notifyChatPort;
    }

    @Override
    @Transactional
    public MessageResponse send(SendGroupMessageCommand command) {
        Destination destination = Destination.group(command.groupId());
        Message message;
        ChatImage image = null;
        if (command.image() == null) {
            message = Message.text(command.senderId(), destination, command.content());
            Access.requireMember(checkGroupMemberPort, command.groupId(), command.senderId());
        } else {
            Access.requireMember(checkGroupMemberPort, command.groupId(), command.senderId());
            image = ChatImage.of(command.image().contentType(), command.image().bytes());
            message = Message.photo(command.senderId(), destination, command.content());
        }
        Replies.attach(loadMessagePort, message, command.replyToMessageId());
        MessageResponse response = messageResponses.one(createMessagePort.create(message, image));
        notifyChatPort.messageSent(response, checkGroupMemberPort.memberIds(command.groupId()));
        return response;
    }
}
