package com.astra.call.domain;

import com.astra.shared.exception.ConflictException;

public class CannotCallYourselfException extends ConflictException {

    public CannotCallYourselfException() {
        super("Não é possível ligar para si mesmo");
    }
}
