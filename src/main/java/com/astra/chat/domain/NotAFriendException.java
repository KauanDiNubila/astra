package com.astra.chat.domain;

import com.astra.shared.exception.ConflictException;

public class NotAFriendException extends ConflictException {

    public NotAFriendException() {
        super("Só é possível adicionar amigos ao grupo");
    }
}
