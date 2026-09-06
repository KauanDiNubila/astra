package com.astra.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 120) @Pattern(regexp = "^[^#]*$", message = "Nome não pode conter '#'") String name,
        @Size(max = 80) String bio,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$", message = "Cor precisa estar no formato #rrggbb") String accentColor,
        @Pattern(regexp = "^(SPARKLES|CONFETTI|SNOW)$", message = "Efeito inválido") String profileEffect) {
}
