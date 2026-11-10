package com.astra.chat.domain;

import com.astra.shared.exception.ConflictException;

public class PinLimitReachedException extends ConflictException {

    public PinLimitReachedException(int limit) {
        super("Dá pra fixar até " + limit + " mensagens por conversa. Desafixe uma antes.");
    }
}
