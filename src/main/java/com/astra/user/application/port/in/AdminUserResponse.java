package com.astra.user.application.port.in;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AdminUserResponse(UUID id, String name, String email, String role, boolean banned,
                                OffsetDateTime createdAt) {
}
