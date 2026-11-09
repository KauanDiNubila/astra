package com.astra.social.domain;

import com.astra.shared.exception.ConflictException;

public class FriendRequestAlreadySentException extends ConflictException {

    public FriendRequestAlreadySentException() {
        super("Convite já enviado");
    }
}
