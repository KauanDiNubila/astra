package com.astra.call.domain;

import com.astra.shared.exception.ConflictException;

public class InvalidSignalException extends ConflictException {

    public InvalidSignalException() {
        super("Sinal inválido");
    }
}
