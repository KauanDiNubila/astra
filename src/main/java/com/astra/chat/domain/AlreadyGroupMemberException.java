package com.astra.chat.domain;

import com.astra.shared.exception.ConflictException;

public class AlreadyGroupMemberException extends ConflictException {

    public AlreadyGroupMemberException() {
        super("Usuário já está no grupo");
    }
}
