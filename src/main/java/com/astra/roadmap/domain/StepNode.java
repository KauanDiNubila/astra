package com.astra.roadmap.domain;

import java.util.UUID;

public record StepNode(UUID id, UUID parentStepId, int position) {

    public boolean isMilestone() {
        return parentStepId == null;
    }
}
