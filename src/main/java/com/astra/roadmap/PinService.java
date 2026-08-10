package com.astra.roadmap;

import com.astra.learning.CourseService;
import com.astra.roadmap.dto.PinRequest;
import com.astra.roadmap.dto.PinResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PinService {

    private final CourseStepLinkRepository linkRepository;
    private final RoadmapStepRepository stepRepository;
    private final CourseService courseService;
    private final CurrentUserProvider currentUserProvider;

    public PinService(CourseStepLinkRepository linkRepository, RoadmapStepRepository stepRepository,
                      CourseService courseService, CurrentUserProvider currentUserProvider) {
        this.linkRepository = linkRepository;
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

    @Transactional(readOnly = true)
    public List<PinResponse> listForStep(UUID stepId) {
        UUID userId = currentUserProvider.currentUserId();
        accessibleStep(stepId, userId);
        List<UUID> courseIds = courseService.courseIdsForUser(userId);
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return linkRepository.findByStepIdAndCourseIdIn(stepId, courseIds).stream()
                .map(this::toDto)
                .toList();
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
