package com.astra.user.dto;

public record AuthResponse(String accessToken, UserResponse user) {
}
