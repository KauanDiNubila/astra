package com.astra.call.domain;

import com.astra.shared.exception.ConflictException;

public class CallFullException extends ConflictException {

    public CallFullException(int max) {
        super("A call está cheia (máximo " + max + " pessoas)");
    }
}
