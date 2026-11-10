package com.astra.call.domain;

import com.astra.shared.exception.ConflictException;

public class AlreadyInCallOnAnotherTabException extends ConflictException {

    public AlreadyInCallOnAnotherTabException() {
        super("Você já está nessa call em outra aba");
    }
}
