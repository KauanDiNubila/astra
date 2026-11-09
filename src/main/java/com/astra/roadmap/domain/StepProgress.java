package com.astra.roadmap.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StepProgress(UUID userId, UUID stepId, StepStatus status, OffsetDateTime completedAt) {
}
