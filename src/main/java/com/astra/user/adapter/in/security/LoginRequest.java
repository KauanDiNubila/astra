package com.astra.user.adapter.in.security;

import com.astra.user.application.port.in.LogInCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {

    LogInCommand toCommand() {
        return new LogInCommand(email, password);
    }
}
