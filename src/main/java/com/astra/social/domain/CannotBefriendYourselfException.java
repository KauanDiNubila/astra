package com.astra.social.domain;

import com.astra.shared.exception.ConflictException;

public class CannotBefriendYourselfException extends ConflictException {

    public CannotBefriendYourselfException() {
        super("Não é possível adicionar você mesmo");
    }
}
