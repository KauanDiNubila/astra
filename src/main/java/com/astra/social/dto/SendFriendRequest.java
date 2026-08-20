package com.astra.social.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendFriendRequest(
        @NotBlank @Email String email
) {
}
