package com.astra.call.domain;

import java.util.UUID;

public record ActiveCall(UUID callId, UUID groupId, int participantCount) {
}
