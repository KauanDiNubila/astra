package com.astra.roadmap.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record RoadmapProgress(
        int milestones,
        int topics,
        int done,
        int total,
        boolean started,
        List<MilestoneState> milestoneStates
) {

    public static RoadmapProgress of(List<StepNode> steps, Map<UUID, StepStatus> statusByStep) {
        List<StepNode> milestones = steps.stream()
                .filter(StepNode::isMilestone)
                .sorted(Comparator.comparingInt(StepNode::position))
                .toList();
        List<StepStatus> statuses = steps.stream().map(step -> statusByStep.get(step.id())).toList();
        int total = (int) statuses.stream().filter(status -> status != StepStatus.SKIPPED).count();
        int done = (int) statuses.stream().filter(status -> status == StepStatus.DONE).count();
        boolean started = statuses.stream().anyMatch(RoadmapProgress::isProgress);
        List<MilestoneState> states = milestones.stream()
                .map(milestone -> stateOf(milestone, steps, statusByStep))
                .toList();
        return new RoadmapProgress(milestones.size(), steps.size() - milestones.size(), done, total, started,
                states);
    }

    private static MilestoneState stateOf(StepNode milestone, List<StepNode> steps,
                                          Map<UUID, StepStatus> statusByStep) {
        List<StepStatus> children = steps.stream()
                .filter(step -> milestone.id().equals(step.parentStepId()))
                .map(step -> statusByStep.get(step.id()))
                .filter(status -> status != StepStatus.SKIPPED)
                .toList();
        StepStatus own = statusByStep.get(milestone.id());
        boolean allChildrenDone = !children.isEmpty() && children.stream().allMatch(s -> s == StepStatus.DONE);
        if (own == StepStatus.DONE || allChildrenDone) {
            return MilestoneState.DONE;
        }
        if (own == StepStatus.LEARNING || children.stream().anyMatch(RoadmapProgress::isProgress)) {
            return MilestoneState.ACTIVE;
        }
        return MilestoneState.TODO;
    }

    private static boolean isProgress(StepStatus status) {
        return status == StepStatus.DONE || status == StepStatus.LEARNING;
    }
}
