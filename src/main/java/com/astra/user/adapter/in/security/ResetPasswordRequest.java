package com.astra.user.adapter.in.security;

import com.astra.user.application.port.in.ResetPasswordCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ResetPasswordRequest(
        @NotBlank @Size(max = 64) String token,
        @NotBlank @Size(min = ResetPasswordCommand.PASSWORD_MIN_LENGTH, max = ResetPasswordCommand.PASSWORD_MAX_LENGTH)
        String newPassword
) {

    ResetPasswordCommand toCommand() {
        return new ResetPasswordCommand(token, newPassword);
    }
}
