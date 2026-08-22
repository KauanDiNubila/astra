package com.astra.roadmap;

import com.astra.roadmap.dto.CreateRoadmapRequest;
import com.astra.roadmap.dto.CreateStepRequest;
import com.astra.roadmap.dto.RoadmapDetailResponse;
import com.astra.roadmap.dto.RoadmapResponse;
import com.astra.roadmap.dto.StepResponse;
import com.astra.roadmap.dto.UpdateStepRequest;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository stepRepository;
    private final RoadmapStepCompletionRepository completionRepository;
    private final CurrentUserProvider currentUserProvider;

    public RoadmapService(RoadmapRepository roadmapRepository, RoadmapStepRepository stepRepository,
                          RoadmapStepCompletionRepository completionRepository,
                          CurrentUserProvider currentUserProvider) {
        this.roadmapRepository = roadmapRepository;
        this.stepRepository = stepRepository;
        this.completionRepository = completionRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public RoadmapResponse create(CreateRoadmapRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        Roadmap roadmap = new Roadmap(userId, request.title(), request.source());
        return toSummary(roadmapRepository.save(roadmap));
    }

    @Transactional(readOnly = true)
    public List<RoadmapResponse> list() {
        UUID userId = currentUserProvider.currentUserId();
        return roadmapRepository.findByOwnerIdIsNullOrOwnerId(userId).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public RoadmapDetailResponse get(UUID roadmapId) {
        UUID userId = currentUserProvider.currentUserId();
        Roadmap roadmap = accessibleRoadmap(roadmapId);
        List<RoadmapStep> steps = stepRepository.findByRoadmapIdOrderByPosition(roadmapId);
        Set<UUID> completedStepIds = steps.isEmpty()
                ? Set.of()
                : Set.copyOf(completionRepository.findCompletedStepIds(userId,
                        steps.stream().map(RoadmapStep::getId).toList()));
        List<StepResponse> stepResponses = steps.stream()
                .map(step -> toStepResponse(step, completedStepIds.contains(step.getId())))
                .toList();
        return new RoadmapDetailResponse(roadmap.getId(), roadmap.getTitle(), roadmap.getSource(),
                roadmap.getOwnerId() == null, stepResponses);
    }

    @Transactional
    public StepResponse addStep(UUID roadmapId, CreateStepRequest request) {
        Roadmap roadmap = ownedRoadmap(roadmapId);
        UUID parentStepId = request.parentStepId();
        if (parentStepId != null) {
            RoadmapStep parent = stepRepository.findById(parentStepId)
                    .orElseThrow(() -> new NotFoundException("Parent step not found"));
            if (!parent.getRoadmap().getId().equals(roadmapId)) {
                throw new NotFoundException("Parent step not found");
            }
        }
        RoadmapStep step = new RoadmapStep(roadmap, request.title(), request.position(), parentStepId);
        RoadmapStep saved = stepRepository.save(step);
        return toStepResponse(saved, false);
    }

    @Transactional
    public StepResponse setStepCompleted(UUID roadmapId, UUID stepId, UpdateStepRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        accessibleRoadmap(roadmapId);
        RoadmapStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new NotFoundException("Step not found"));
        if (!step.getRoadmap().getId().equals(roadmapId)) {
            throw new NotFoundException("Step not found");
        }
        boolean exists = completionRepository.existsByUserIdAndStepId(userId, stepId);
        if (request.completed() && !exists) {
            completionRepository.save(new RoadmapStepCompletion(userId, stepId));
        } else if (!request.completed() && exists) {
            completionRepository.deleteByUserIdAndStepId(userId, stepId);
        }
        return toStepResponse(step, request.completed());
    }

    private Roadmap accessibleRoadmap(UUID roadmapId) {
        UUID userId = currentUserProvider.currentUserId();
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new NotFoundException("Roadmap not found"));
        if (roadmap.getOwnerId() != null && !roadmap.getOwnerId().equals(userId)) {
            throw new NotFoundException("Roadmap not found");
        }
        return roadmap;
    }

    private Roadmap ownedRoadmap(UUID roadmapId) {
        UUID userId = currentUserProvider.currentUserId();
        return roadmapRepository.findByIdAndOwnerId(roadmapId, userId)
                .orElseThrow(() -> new NotFoundException("Roadmap not found"));
    }

    private RoadmapResponse toSummary(Roadmap roadmap) {
        return new RoadmapResponse(roadmap.getId(), roadmap.getTitle(), roadmap.getSource(),
                roadmap.getOwnerId() == null);
    }

    private StepResponse toStepResponse(RoadmapStep step, boolean completed) {
        return new StepResponse(step.getId(), step.getTitle(), step.getPosition(), step.getParentStepId(),
                completed);
    }
}
