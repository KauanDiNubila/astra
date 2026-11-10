package com.astra.call.domain;

import com.astra.shared.exception.ConflictException;

public class CalleeBusyException extends ConflictException {

    public CalleeBusyException() {
        super("Essa pessoa está em outra call");
    }
}
