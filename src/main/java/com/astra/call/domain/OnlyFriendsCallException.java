package com.astra.call.domain;

import com.astra.shared.exception.ConflictException;

public class OnlyFriendsCallException extends ConflictException {

    public OnlyFriendsCallException() {
        super("Você só pode ligar para amigos");
    }
}
