package com.astra.chat.application.service;

import com.astra.chat.application.port.in.MessageResponse;
import com.astra.chat.application.port.in.PinMessageUseCase;
import com.astra.chat.application.port.out.CheckFriendshipPort;
import com.astra.chat.application.port.out.CheckGroupMemberPort;
import com.astra.chat.application.port.out.ListMessagesPort;
import com.astra.chat.application.port.out.LoadMessagePort;
import com.astra.chat.application.port.out.NotifyChatPort;
import com.astra.chat.application.port.out.UpdateMessagePinPort;
import com.astra.chat.domain.Message;
import com.astra.shared.exception.NotFoundException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class PinMessageService implements PinMessageUseCase {

    private final LoadMessagePort loadMessagePort;
    private final ListMessagesPort listMessagesPort;
    private final UpdateMessagePinPort updateMessagePinPort;
    private final CheckFriendshipPort checkFriendshipPort;
    private final CheckGroupMemberPort checkGroupMemberPort;
    private final MessageResponses messageResponses;
    private final NotifyChatPort notifyChatPort;
    private final Clock clock;

    PinMessageService(LoadMessagePort loadMessagePort, ListMessagesPort listMessagesPort,
                      UpdateMessagePinPort updateMessagePinPort, CheckFriendshipPort checkFriendshipPort,
                      CheckGroupMemberPort checkGroupMemberPort, MessageResponses messageResponses,
                      NotifyChatPort notifyChatPort, Clock clock) {
        this.loadMessagePort = loadMessagePort;
        this.listMessagesPort = listMessagesPort;
        this.updateMessagePinPort = updateMessagePinPort;
        this.checkFriendshipPort = checkFriendshipPort;
        this.checkGroupMemberPort = checkGroupMemberPort;
        this.messageResponses = messageResponses;
        this.notifyChatPort = notifyChatPort;
        this.clock = clock;
    }

    @Override
    @Transactional
    public MessageResponse setPinned(UUID userId, UUID messageId, boolean pinned) {
        Message message = loadMessagePort.load(messageId)
                .orElseThrow(() -> new NotFoundException("Mensagem não encontrada"));
        List<UUID> audience;
        int alreadyPinned;
        if (message.getGroupId() != null) {
            Access.requireMember(checkGroupMemberPort, message.getGroupId(), userId);
            audience = checkGroupMemberPort.memberIds(message.getGroupId());
            alreadyPinned = listMessagesPort.pinnedInGroup(message.getGroupId()).size();
        } else {
            if (!message.involves(userId)) {
                throw new NotFoundException("Mensagem não encontrada");
            }
            UUID other = message.otherParticipant(userId);
            Access.requireFriends(checkFriendshipPort, userId, other);
            audience = List.of(message.getSenderId(), message.getRecipientId());
            alreadyPinned = listMessagesPort.pinnedDirect(userId, other).size();
        }
        if (pinned) {
            message.pin(userId, OffsetDateTime.now(clock), alreadyPinned);
        } else {
            message.unpin();
        }
        updateMessagePinPort.updatePin(message);
        MessageResponse response = messageResponses.one(message);
        notifyChatPort.messageChanged(response, audience.stream().distinct().toList());
        return response;
    }
}
