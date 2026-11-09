package com.astra.roadmap.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class RoadmapStep {

    private final UUID id;
    private final String title;
    private final int position;
    private final UUID parentStepId;
    private final String description;
    private final List<StepResource> resources;

    private RoadmapStep(UUID id, String title, int position, UUID parentStepId, String description,
                        List<StepResource> resources) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.position = position;
        this.parentStepId = parentStepId;
        this.description = description;
        this.resources = new ArrayList<>(resources);
        this.resources.sort(Comparator.comparingInt(StepResource::getPosition));
    }

    static RoadmapStep create(String title, int position, UUID parentStepId, String description) {
        return new RoadmapStep(UUID.randomUUID(), title, position, parentStepId, description, List.of());
    }

    public static RoadmapStep withId(UUID id, String title, int position, UUID parentStepId, String description,
                                     List<StepResource> resources) {
        return new RoadmapStep(id, title, position, parentStepId, description, resources);
    }

    public List<StepResource> getResources() {
        return Collections.unmodifiableList(resources);
    }

    public StepNode node() {
        return new StepNode(id, parentStepId, position);
    }

    StepResource addResource(String label, String url) {
        StepResource resource = StepResource.create(label, url, resources.size());
        resources.add(resource);
        return resource;
    }

    void removeResource(UUID resourceId) {
        resources.removeIf(r -> r.getId().equals(resourceId));
    }
}
