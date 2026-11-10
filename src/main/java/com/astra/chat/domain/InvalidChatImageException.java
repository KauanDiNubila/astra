package com.astra.chat.domain;

import com.astra.shared.exception.ConflictException;

public class InvalidChatImageException extends ConflictException {

    public InvalidChatImageException(String message) {
        super(message);
    }
}
