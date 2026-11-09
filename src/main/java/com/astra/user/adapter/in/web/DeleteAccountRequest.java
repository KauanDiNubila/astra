package com.astra.user.adapter.in.web;

import com.astra.user.application.port.in.DeleteOwnAccountCommand;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record DeleteAccountRequest(@Size(max = 100) String password, @Size(max = 180) String confirmation) {

    DeleteOwnAccountCommand toCommand(UUID userId) {
        return new DeleteOwnAccountCommand(userId, password, confirmation);
    }
}
