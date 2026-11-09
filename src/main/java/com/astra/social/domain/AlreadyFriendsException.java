package com.astra.social.domain;

import com.astra.shared.exception.ConflictException;

public class AlreadyFriendsException extends ConflictException {

    public AlreadyFriendsException() {
        super("Vocês já são amigos");
    }
}
