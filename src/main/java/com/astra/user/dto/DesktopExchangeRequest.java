package com.astra.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DesktopExchangeRequest(
        @NotBlank @Size(max = 128) String code,
        @NotBlank @Size(min = 43, max = 128) String verifier
) {
}
