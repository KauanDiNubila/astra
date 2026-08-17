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
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository stepRepository;
    private final CurrentUserProvider currentUserProvider;

    public RoadmapService(RoadmapRepository roadmapRepository, RoadmapStepRepository stepRepository,
                          CurrentUserProvider currentUserProvider) {
        this.roadmapRepository = roadmapRepository;
        this.stepRepository = stepRepository;
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
        Roadmap roadmap = accessibleRoadmap(roadmapId);
        List<StepResponse> steps = stepRepository.findByRoadmapIdOrderByPosition(roadmapId).stream()
                .map(this::toStepResponse)
                .toList();
        return new RoadmapDetailResponse(roadmap.getId(), roadmap.getTitle(), roadmap.getSource(),
                roadmap.getOwnerId() == null, steps);
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
        return toStepResponse(saved);
    }

    @Transactional
    public StepResponse setStepCompleted(UUID roadmapId, UUID stepId, UpdateStepRequest request) {
        accessibleRoadmap(roadmapId);
        RoadmapStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new NotFoundException("Step not found"));
        if (!step.getRoadmap().getId().equals(roadmapId)) {
            throw new NotFoundException("Step not found");
        }
        step.setCompleted(request.completed());
        RoadmapStep saved = stepRepository.save(step);
        return toStepResponse(saved);
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

    private StepResponse toStepResponse(RoadmapStep step) {
        return new StepResponse(step.getId(), step.getTitle(), step.getPosition(), step.getParentStepId(),
                step.isCompleted());
    }
}
