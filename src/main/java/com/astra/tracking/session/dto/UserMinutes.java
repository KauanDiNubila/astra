package com.astra.tracking.session.dto;

import java.util.UUID;

public record UserMinutes(UUID userId, long minutes) {
}
