package com.astra.github.domain;

import com.astra.shared.exception.ConflictException;

public class InvalidGitHubConnectionException extends ConflictException {

    public InvalidGitHubConnectionException() {
        super("Conexão com o GitHub inválida — reconecte");
    }
}
