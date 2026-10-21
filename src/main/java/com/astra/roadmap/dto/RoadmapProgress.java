package com.astra.roadmap.dto;

import java.util.List;

public record RoadmapProgress(
        int milestones,
        int topics,
        int done,
        int total,
        boolean started,
        List<MilestoneState> milestoneStates
) {

    public static final RoadmapProgress EMPTY = new RoadmapProgress(0, 0, 0, 0, false, List.of());

    public enum MilestoneState {
        DONE,
        ACTIVE,
        TODO
    }
}
