package com.astra.roadmap.adapter.out.persistence;

import com.astra.roadmap.domain.LocatedStep;
import com.astra.roadmap.domain.Roadmap;
import com.astra.roadmap.domain.RoadmapOutline;
import com.astra.roadmap.domain.RoadmapStep;
import com.astra.roadmap.domain.StepNode;
import com.astra.roadmap.domain.StepResource;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class RoadmapMapper {

    Roadmap toDomain(RoadmapJpaEntity entity) {
        return Roadmap.withId(entity.getId(), entity.getOwnerId(), entity.getTitle(), entity.getSource(),
                entity.getSteps().stream().map(this::toDomain).toList());
    }

    RoadmapOutline toOutline(RoadmapJpaEntity entity) {
        return new RoadmapOutline(entity.getId(), entity.getOwnerId(), entity.getTitle(), entity.getSource(),
                entity.getSteps().stream()
                        .map(s -> new StepNode(s.getId(), s.getParentStepId(), s.getPosition()))
                        .toList());
    }

    LocatedStep toLocated(StepJpaEntity step) {
        return new LocatedStep(step.getRoadmap().getId(), step.getRoadmap().getOwnerId(), toDomain(step));
    }

    RoadmapJpaEntity toNewJpaEntity(Roadmap roadmap) {
        RoadmapJpaEntity entity = new RoadmapJpaEntity(roadmap.getId(), roadmap.getOwnerId(), roadmap.getTitle(),
                roadmap.getSource(), new ArrayList<>());
        copySteps(roadmap, entity);
        return entity;
    }

    void copySteps(Roadmap roadmap, RoadmapJpaEntity entity) {
        Map<UUID, StepJpaEntity> existing = entity.getSteps().stream()
                .collect(Collectors.toMap(StepJpaEntity::getId, Function.identity()));
        for (RoadmapStep step : roadmap.getSteps()) {
            StepJpaEntity target = existing.get(step.getId());
            if (target == null) {
                target = new StepJpaEntity(step.getId(), entity, step.getTitle(), step.getPosition(),
                        step.getParentStepId(), step.getDescription());
                entity.getSteps().add(target);
            }
            copyResources(step, target);
        }
    }

    private RoadmapStep toDomain(StepJpaEntity step) {
        return RoadmapStep.withId(step.getId(), step.getTitle(), step.getPosition(), step.getParentStepId(),
                step.getDescription(), step.getResources().stream()
                        .map(r -> StepResource.withId(r.getId(), r.getLabel(), r.getUrl(), r.getPosition()))
                        .toList());
    }

    private void copyResources(RoadmapStep step, StepJpaEntity target) {
        Set<UUID> kept = step.getResources().stream().map(StepResource::getId).collect(Collectors.toSet());
        target.getResources().removeIf(r -> !kept.contains(r.getId()));
        Set<UUID> present = target.getResources().stream().map(ResourceJpaEntity::getId).collect(Collectors.toSet());
        for (StepResource resource : step.getResources()) {
            if (!present.contains(resource.getId())) {
                target.getResources().add(new ResourceJpaEntity(resource.getId(), target, resource.getLabel(),
                        resource.getUrl(), resource.getPosition()));
            }
        }
    }
}
