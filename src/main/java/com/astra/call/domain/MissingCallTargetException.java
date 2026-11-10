package com.astra.call.domain;

import com.astra.shared.exception.ConflictException;

public class MissingCallTargetException extends ConflictException {

    public MissingCallTargetException() {
        super("Informe um amigo ou um grupo");
    }
}
