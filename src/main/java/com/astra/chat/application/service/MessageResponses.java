package com.astra.chat.application.service;

import com.astra.chat.application.port.in.MessageResponse;
import com.astra.chat.application.port.in.MessageResponse.ReplyPreview;
import com.astra.chat.application.port.out.LoadAttachmentPort;
import com.astra.chat.application.port.out.LoadMessagePort;
import com.astra.chat.domain.Message;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class MessageResponses {

    private final LoadAttachmentPort loadAttachmentPort;
    private final LoadMessagePort loadMessagePort;

    MessageResponses(LoadAttachmentPort loadAttachmentPort, LoadMessagePort loadMessagePort) {
        this.loadAttachmentPort = loadAttachmentPort;
        this.loadMessagePort = loadMessagePort;
    }

    MessageResponse one(Message message) {
        return many(List.of(message)).getFirst();
    }

    List<MessageResponse> many(List<Message> messages) {
        Map<UUID, UUID> attachmentByMessage = loadAttachmentPort.attachmentIdsFor(
                messages.stream().map(Message::getId).toList());
        List<UUID> replyIds = messages.stream()
                .map(Message::getReplyToMessageId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        Map<UUID, Message> originals = replyIds.isEmpty() ? Map.of()
                : loadMessagePort.loadAll(replyIds).stream()
                        .collect(Collectors.toMap(Message::getId, Function.identity()));
        return messages.stream()
                .sorted(Comparator.comparing(Message::getCreatedAt))
                .map(m -> toResponse(m, attachmentByMessage.get(m.getId()),
                        m.getReplyToMessageId() == null ? null : originals.get(m.getReplyToMessageId())))
                .toList();
    }

    private static MessageResponse toResponse(Message message, UUID attachmentId, Message original) {
        ReplyPreview replyTo = original == null ? null
                : new ReplyPreview(original.getId(), original.getSenderId(), original.preview());
        return new MessageResponse(message.getId(), message.getSenderId(), message.getRecipientId(),
                message.getGroupId(), message.getContent(), message.getCreatedAt(), message.isRead(), attachmentId,
                replyTo, message.getPinnedAt(), message.getPinnedBy());
    }
}
