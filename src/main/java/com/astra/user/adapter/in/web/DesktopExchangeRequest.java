package com.astra.user.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record DesktopExchangeRequest(
        @NotBlank @Size(max = 128) String code,
        @NotBlank @Size(min = 43, max = 128) String verifier
) {
}
