package com.astra.call.domain;

import com.astra.shared.exception.ConflictException;

public class AlreadyInCallException extends ConflictException {

    public AlreadyInCallException() {
        super("Você já está em uma call");
    }
}
