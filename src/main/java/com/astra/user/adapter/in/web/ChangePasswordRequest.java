package com.astra.user.adapter.in.web;

import com.astra.user.application.port.in.ChangePasswordCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record ChangePasswordRequest(
        @NotBlank String currentPassword,
        @NotBlank @Size(min = ChangePasswordCommand.PASSWORD_MIN_LENGTH, max = ChangePasswordCommand.PASSWORD_MAX_LENGTH)
        String newPassword
) {

    ChangePasswordCommand toCommand(UUID userId) {
        return new ChangePasswordCommand(userId, currentPassword, newPassword);
    }
}
