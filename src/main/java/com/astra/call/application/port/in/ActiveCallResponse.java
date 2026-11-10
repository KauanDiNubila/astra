package com.astra.call.application.port.in;

import java.util.UUID;

public record ActiveCallResponse(UUID callId, UUID groupId, int participantCount) {
}
