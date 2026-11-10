package com.astra.chat.domain;

import com.astra.shared.exception.ConflictException;

public class EmptyMessageException extends ConflictException {

    public EmptyMessageException() {
        super("Mensagem precisa ter texto");
    }
}
