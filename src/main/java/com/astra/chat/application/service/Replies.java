package com.astra.chat.application.service;

import com.astra.chat.application.port.out.LoadMessagePort;
import com.astra.chat.domain.Message;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;

final class Replies {

    private Replies() {
    }

    static void attach(LoadMessagePort loadMessagePort, Message message, UUID replyToMessageId) {
        if (replyToMessageId != null) {
            message.replyTo(loadMessagePort.load(replyToMessageId)
                    .orElseThrow(() -> new NotFoundException("Mensagem original não encontrada")));
        }
    }
}
