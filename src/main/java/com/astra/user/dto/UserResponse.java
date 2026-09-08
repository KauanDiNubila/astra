package com.astra.user.dto;

import java.util.UUID;

public record UserResponse(UUID id, String name, String email, String bio, String role, String tag) {
}
