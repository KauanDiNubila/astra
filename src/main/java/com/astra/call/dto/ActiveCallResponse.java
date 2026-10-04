package com.astra.call.dto;

import java.util.UUID;

public record ActiveCallResponse(UUID callId, UUID groupId, int participantCount) {
}
