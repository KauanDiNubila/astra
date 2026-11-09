package com.astra.roadmap.domain;

import com.astra.shared.exception.NotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class Roadmap {

    private final UUID id;
    private final UUID ownerId;
    private final String title;
    private final String source;
    private final List<RoadmapStep> steps;

    private Roadmap(UUID id, UUID ownerId, String title, String source, List<RoadmapStep> steps) {
        this.id = Objects.requireNonNull(id);
        this.ownerId = ownerId;
        this.title = Objects.requireNonNull(title);
        this.source = source;
        this.steps = new ArrayList<>(steps);
        this.steps.sort(Comparator.comparingInt(RoadmapStep::getPosition));
    }

    public static Roadmap create(UUID ownerId, String title, String source) {
        return new Roadmap(UUID.randomUUID(), Objects.requireNonNull(ownerId), title, source, List.of());
    }

    public static Roadmap withId(UUID id, UUID ownerId, String title, String source, List<RoadmapStep> steps) {
        return new Roadmap(id, ownerId, title, source, steps);
    }

    public List<RoadmapStep> getSteps() {
        return Collections.unmodifiableList(steps);
    }

    public RoadmapOutline outline() {
        return new RoadmapOutline(id, ownerId, title, source, steps.stream().map(RoadmapStep::node).toList());
    }

    public RoadmapStep step(UUID stepId) {
        return steps.stream()
                .filter(s -> s.getId().equals(stepId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Step not found"));
    }

    public RoadmapStep addStep(String title, int position, UUID parentStepId, String description) {
        if (parentStepId != null && steps.stream().noneMatch(s -> s.getId().equals(parentStepId))) {
            throw new NotFoundException("Parent step not found");
        }
        RoadmapStep step = RoadmapStep.create(title, position, parentStepId, description);
        steps.add(step);
        steps.sort(Comparator.comparingInt(RoadmapStep::getPosition));
        return step;
    }

    public StepResource addResource(UUID stepId, String label, String url) {
        return step(stepId).addResource(label, url);
    }

    public void removeResource(UUID stepId, UUID resourceId) {
        step(stepId).removeResource(resourceId);
    }
}
