package com.astra.roadmap;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class RoadmapStepCompletionId implements Serializable {

    private UUID userId;
    private UUID stepId;

    public RoadmapStepCompletionId() {
    }

    public RoadmapStepCompletionId(UUID userId, UUID stepId) {
        this.userId = userId;
        this.stepId = stepId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoadmapStepCompletionId that)) {
            return false;
        }
        return Objects.equals(userId, that.userId) && Objects.equals(stepId, that.stepId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, stepId);
    }
}
