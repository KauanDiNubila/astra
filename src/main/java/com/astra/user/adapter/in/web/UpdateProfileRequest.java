package com.astra.user.adapter.in.web;

import com.astra.user.application.port.in.UpdateProfileCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record UpdateProfileRequest(
        @NotBlank @Size(max = UpdateProfileCommand.NAME_MAX_LENGTH)
        @Pattern(regexp = "^[^#]*$", message = "Nome não pode conter '#'") String name,
        @Size(max = UpdateProfileCommand.BIO_MAX_LENGTH) String bio
) {

    UpdateProfileCommand toCommand(UUID userId) {
        return new UpdateProfileCommand(userId, name, bio);
    }
}
