package com.astra.user.adapter.in.security;

import com.astra.user.application.port.in.RegisterUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record ForgotPasswordRequest(@NotBlank @Email @Size(max = RegisterUserCommand.EMAIL_MAX_LENGTH) String email) {
}
