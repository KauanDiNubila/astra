package com.astra.github.domain;

import com.astra.shared.exception.ConflictException;

public class SyncTooSoonException extends ConflictException {

    public SyncTooSoonException() {
        super("Aguarde alguns minutos antes de sincronizar de novo");
    }
}
