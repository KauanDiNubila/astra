package com.astra.chat.domain;

import com.astra.shared.exception.ConflictException;

public class ReplyOutsideConversationException extends ConflictException {

    public ReplyOutsideConversationException() {
        super("Mensagem não pertence a esta conversa");
    }
}
