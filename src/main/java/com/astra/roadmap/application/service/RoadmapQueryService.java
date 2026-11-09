package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.ExportRoadmapsQuery;
import com.astra.roadmap.application.port.in.GetRoadmapQuery;
import com.astra.roadmap.application.port.in.ListRoadmapsQuery;
import com.astra.roadmap.application.port.in.RoadmapDetailResponse;
import com.astra.roadmap.application.port.in.RoadmapResponse;
import com.astra.roadmap.application.port.out.ListRoadmapsPort;
import com.astra.roadmap.application.port.out.LoadRoadmapPort;
import com.astra.roadmap.application.port.out.LoadStepStatusesPort;
import com.astra.roadmap.domain.Roadmap;
import com.astra.roadmap.domain.RoadmapOutline;
import com.astra.roadmap.domain.StepStatus;
import com.astra.shared.ExportRow;
import com.astra.shared.exception.NotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class RoadmapQueryService implements ListRoadmapsQuery, GetRoadmapQuery, ExportRoadmapsQuery {

    private final ListRoadmapsPort listRoadmapsPort;
    private final LoadRoadmapPort loadRoadmapPort;
    private final LoadStepStatusesPort loadStepStatusesPort;

    RoadmapQueryService(ListRoadmapsPort listRoadmapsPort, LoadRoadmapPort loadRoadmapPort, LoadStepStatusesPort loadStepStatusesPort) {
        this.listRoadmapsPort = listRoadmapsPort;
        this.loadRoadmapPort = loadRoadmapPort;
        this.loadStepStatusesPort = loadStepStatusesPort;
    }

    @Override
    public List<RoadmapResponse> list(UUID userId) {
        List<RoadmapOutline> roadmaps = listRoadmapsPort.listOutlinesFor(userId);
        Map<UUID, StepStatus> statuses = statusesOf(userId,
                roadmaps.stream().flatMap(r -> r.stepIds().stream()).toList());
        return roadmaps.stream()
                .map(roadmap -> RoadmapResponse.from(roadmap, roadmap.progress(statuses)))
                .toList();
    }

    @Override
    public RoadmapDetailResponse get(UUID userId, UUID roadmapId) {
        Roadmap roadmap = loadRoadmapPort.load(roadmapId)
                .filter(r -> r.outline().isAccessibleBy(userId))
                .orElseThrow(() -> new NotFoundException("Roadmap not found"));
        return RoadmapDetailResponse.from(roadmap, statusesOf(userId, roadmap.outline().stepIds()));
    }

    @Override
    public Map<String, Object> exportRoadmaps(UUID userId) {
        List<Map<String, Object>> own = listRoadmapsPort.listOwnedBy(userId).stream()
                .map(roadmap -> ExportRow.of("id", roadmap.getId(), "title", roadmap.getTitle(),
                        "source", roadmap.getSource(),
                        "steps", roadmap.getSteps().stream()
                                .map(step -> ExportRow.of("id", step.getId(), "title", step.getTitle(),
                                        "position", step.getPosition(), "parentStepId", step.getParentStepId(),
                                        "description", step.getDescription()))
                                .toList()))
                .toList();
        List<Map<String, Object>> progress = loadStepStatusesPort.listByUser(userId).stream()
                .map(p -> ExportRow.of("stepId", p.stepId(), "status", p.status(), "completedAt", p.completedAt()))
                .toList();
        return ExportRow.of("ownRoadmaps", own, "stepProgress", progress);
    }

    private Map<UUID, StepStatus> statusesOf(UUID userId, Collection<UUID> stepIds) {
        return stepIds.isEmpty() ? Map.of() : loadStepStatusesPort.statusesOf(userId, stepIds);
    }
}
