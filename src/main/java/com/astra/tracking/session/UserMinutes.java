package com.astra.tracking.session;

import java.util.UUID;

public record UserMinutes(UUID userId, long minutes) {
}
