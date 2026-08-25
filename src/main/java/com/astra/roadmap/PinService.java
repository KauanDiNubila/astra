package com.astra.roadmap;

import com.astra.learning.CourseService;
import com.astra.roadmap.dto.PinRequest;
import com.astra.roadmap.dto.PinResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PinService {

    private final CourseStepLinkRepository linkRepository;
    private final RoadmapRepository roadmapRepository;
    private final RoadmapStepRepository stepRepository;
    private final CourseService courseService;
    private final CurrentUserProvider currentUserProvider;

    public PinService(CourseStepLinkRepository linkRepository, RoadmapRepository roadmapRepository,
                      RoadmapStepRepository stepRepository, CourseService courseService,
                      CurrentUserProvider currentUserProvider) {
        this.linkRepository = linkRepository;
        this.roadmapRepository = roadmapRepository;
        this.stepRepository = stepRepository;
        this.courseService = courseService;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public PinResponse pin(UUID stepId, PinRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        RoadmapStep step = accessibleStep(stepId, userId);
        if (!courseService.existsForUser(request.courseId(), userId)) {
            throw new NotFoundException("Course not found");
        }

        CourseStepLink link = linkRepository.findByStepIdAndCourseId(stepId, request.courseId())
                .orElseGet(() -> new CourseStepLink(request.courseId(), step, request.status(), request.rating()));
        link.setStatus(request.status());
        link.setRating(request.rating());
        CourseStepLink saved = linkRepository.save(link);
        return toDto(saved);
    }

    @Transactional
    public void unpin(UUID stepId, UUID pinId) {
        UUID userId = currentUserProvider.currentUserId();
        accessibleStep(stepId, userId);
        CourseStepLink link = linkRepository.findById(pinId)
                .orElseThrow(() -> new NotFoundException("Pin not found"));
        if (!link.getStep().getId().equals(stepId)) {
            throw new NotFoundException("Pin not found");
        }
        if (!courseService.existsForUser(link.getCourseId(), userId)) {
            throw new NotFoundException("Pin not found");
        }
        linkRepository.delete(link);
    }

    @Transactional(readOnly = true)
    public Map<UUID, List<PinResponse>> listForRoadmap(UUID roadmapId) {
        UUID userId = currentUserProvider.currentUserId();
        accessibleRoadmap(roadmapId, userId);
        List<UUID> stepIds = stepRepository.findByRoadmapIdOrderByPosition(roadmapId).stream()
                .map(RoadmapStep::getId)
                .toList();
        if (stepIds.isEmpty()) {
            return Map.of();
        }
        List<UUID> courseIds = courseService.courseIdsForUser(userId);
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return linkRepository.findByStepIdInAndCourseIdIn(stepIds, courseIds).stream()
                .map(this::toDto)
                .collect(Collectors.groupingBy(PinResponse::stepId));
    }

    private void accessibleRoadmap(UUID roadmapId, UUID userId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new NotFoundException("Roadmap not found"));
        if (roadmap.getOwnerId() != null && !roadmap.getOwnerId().equals(userId)) {
            throw new NotFoundException("Roadmap not found");
        }
    }

    private RoadmapStep accessibleStep(UUID stepId, UUID userId) {
        RoadmapStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new NotFoundException("Step not found"));
        UUID owner = step.getRoadmap().getOwnerId();
        if (owner != null && !owner.equals(userId)) {
            throw new NotFoundException("Step not found");
        }
        return step;
    }

    private PinResponse toDto(CourseStepLink link) {
        return new PinResponse(link.getId(), link.getCourseId(), link.getStep().getId(),
                link.getStatus(), link.getRating());
    }
}
