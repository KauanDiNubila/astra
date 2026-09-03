package com.astra.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 120) @Pattern(regexp = "^[^#]*$", message = "Nome não pode conter '#'") String name,
        @Size(max = 80) String bio) {
}
