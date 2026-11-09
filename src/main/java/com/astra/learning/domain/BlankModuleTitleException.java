package com.astra.learning.domain;

import com.astra.shared.exception.ConflictException;

public class BlankModuleTitleException extends ConflictException {

    public BlankModuleTitleException() {
        super("Título do módulo não pode ficar em branco");
    }
}
