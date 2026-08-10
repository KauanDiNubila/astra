package com.astra.roadmap;

import com.astra.roadmap.dto.CreateRoadmapRequest;
import com.astra.roadmap.dto.CreateStepRequest;
import com.astra.roadmap.dto.RoadmapDetailResponse;
import com.astra.roadmap.dto.RoadmapResponse;
import com.astra.roadmap.dto.StepResponse;
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
                .map(s -> new StepResponse(s.getId(), s.getTitle(), s.getPosition()))
                .toList();
        return new RoadmapDetailResponse(roadmap.getId(), roadmap.getTitle(), roadmap.getSource(),
                roadmap.getOwnerId() == null, steps);
    }

    @Transactional
    public StepResponse addStep(UUID roadmapId, CreateStepRequest request) {
        Roadmap roadmap = ownedRoadmap(roadmapId);
        RoadmapStep step = new RoadmapStep(roadmap, request.title(), request.position());
        RoadmapStep saved = stepRepository.save(step);
        return new StepResponse(saved.getId(), saved.getTitle(), saved.getPosition());
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
}
