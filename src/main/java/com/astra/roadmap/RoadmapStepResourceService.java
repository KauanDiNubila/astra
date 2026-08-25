package com.astra.roadmap;

import com.astra.roadmap.dto.AddResourceRequest;
import com.astra.roadmap.dto.ResourceResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadmapStepResourceService {

    private final RoadmapStepResourceRepository resourceRepository;
    private final RoadmapStepRepository stepRepository;
    private final CurrentUserProvider currentUserProvider;

    public RoadmapStepResourceService(RoadmapStepResourceRepository resourceRepository,
                                      RoadmapStepRepository stepRepository,
                                      CurrentUserProvider currentUserProvider) {
        this.resourceRepository = resourceRepository;
        this.stepRepository = stepRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public List<ResourceResponse> list(UUID stepId) {
        accessibleStep(stepId);
        return resourceRepository.findByStepIdOrderByPosition(stepId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ResourceResponse add(UUID stepId, AddResourceRequest request) {
        ownedStep(stepId);
        int nextPosition = resourceRepository.findByStepIdOrderByPosition(stepId).size();
        RoadmapStepResource saved = resourceRepository.save(
                new RoadmapStepResource(stepId, request.label(), request.url(), nextPosition));
        return toDto(saved);
    }

    @Transactional
    public void remove(UUID stepId, UUID resourceId) {
        ownedStep(stepId);
        resourceRepository.deleteByIdAndStepId(resourceId, stepId);
    }

    private RoadmapStep accessibleStep(UUID stepId) {
        UUID userId = currentUserProvider.currentUserId();
        RoadmapStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new NotFoundException("Step not found"));
        UUID owner = step.getRoadmap().getOwnerId();
        if (owner != null && !owner.equals(userId)) {
            throw new NotFoundException("Step not found");
        }
        return step;
    }

    private RoadmapStep ownedStep(UUID stepId) {
        UUID userId = currentUserProvider.currentUserId();
        RoadmapStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new NotFoundException("Step not found"));
        if (!userId.equals(step.getRoadmap().getOwnerId())) {
            throw new NotFoundException("Step not found");
        }
        return step;
    }

    private ResourceResponse toDto(RoadmapStepResource resource) {
        return new ResourceResponse(resource.getId(), resource.getLabel(), resource.getUrl(),
                resource.getPosition());
    }
}
