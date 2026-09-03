package com.astra.social.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SendFriendRequest(
        @NotBlank @Pattern(regexp = "^.{1,120}#\\d{4}$", message = "Use o formato nome#0000") String handle
) {
}
