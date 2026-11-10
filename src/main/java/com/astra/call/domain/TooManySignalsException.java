package com.astra.call.domain;

import com.astra.shared.exception.ConflictException;

public class TooManySignalsException extends ConflictException {

    public TooManySignalsException() {
        super("Muitos sinais em pouco tempo");
    }
}
