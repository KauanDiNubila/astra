package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.ExportCoursePinsQuery;
import com.astra.roadmap.application.port.in.ListRoadmapPinsQuery;
import com.astra.roadmap.application.port.in.PinResponse;
import com.astra.roadmap.application.port.out.ListRoadmapsPort;
import com.astra.roadmap.application.port.out.LoadCoursePinPort;
import com.astra.roadmap.application.port.out.UserCoursesPort;
import com.astra.roadmap.domain.RoadmapOutline;
import com.astra.shared.ExportRow;
import com.astra.shared.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class CoursePinQueryService implements ListRoadmapPinsQuery, ExportCoursePinsQuery {

    private final ListRoadmapsPort listRoadmapsPort;
    private final UserCoursesPort userCoursesPort;
    private final LoadCoursePinPort loadCoursePinPort;

    CoursePinQueryService(ListRoadmapsPort listRoadmapsPort, UserCoursesPort userCoursesPort, LoadCoursePinPort loadCoursePinPort) {
        this.listRoadmapsPort = listRoadmapsPort;
        this.userCoursesPort = userCoursesPort;
        this.loadCoursePinPort = loadCoursePinPort;
    }

    @Override
    public Map<UUID, List<PinResponse>> pins(UUID userId, UUID roadmapId) {
        RoadmapOutline roadmap = listRoadmapsPort.loadOutline(roadmapId)
                .filter(r -> r.isAccessibleBy(userId))
                .orElseThrow(() -> new NotFoundException("Roadmap not found"));
        if (roadmap.steps().isEmpty()) {
            return Map.of();
        }
        List<UUID> courseIds = userCoursesPort.courseIdsOf(userId);
        if (courseIds.isEmpty()) {
            return Map.of();
        }
        return loadCoursePinPort.listOn(roadmap.stepIds(), courseIds).stream()
                .map(PinResponse::from)
                .collect(Collectors.groupingBy(PinResponse::stepId));
    }

    @Override
    public List<Map<String, Object>> exportPins(UUID userId) {
        List<UUID> courseIds = userCoursesPort.courseIdsOf(userId);
        if (courseIds.isEmpty()) {
            return List.of();
        }
        return loadCoursePinPort.listForCourses(courseIds).stream()
                .map(pin -> ExportRow.of("courseId", pin.getCourseId(), "stepId", pin.getStepId(),
                        "status", pin.getStatus(), "rating", pin.getRating()))
                .toList();
    }
}
