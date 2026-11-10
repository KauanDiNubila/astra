package com.astra.user.adapter.in.security;

import com.astra.user.application.port.in.RegisterUserCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

record RegisterRequest(
        @NotBlank @Size(max = RegisterUserCommand.NAME_MAX_LENGTH)
        @Pattern(regexp = "^[^#]*$", message = "Nome não pode conter '#'") String name,
        @NotBlank @Email @Size(max = RegisterUserCommand.EMAIL_MAX_LENGTH) String email,
        @NotBlank @Size(min = RegisterUserCommand.PASSWORD_MIN_LENGTH, max = RegisterUserCommand.PASSWORD_MAX_LENGTH)
        String password,
        Boolean acceptTerms
) {

    RegisterUserCommand toCommand() {
        return new RegisterUserCommand(name, email, password, Boolean.TRUE.equals(acceptTerms));
    }
}
