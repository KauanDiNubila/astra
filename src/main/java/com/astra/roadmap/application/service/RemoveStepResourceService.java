package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.RemoveStepResourceUseCase;
import com.astra.roadmap.application.port.out.LoadRoadmapPort;
import com.astra.roadmap.application.port.out.UpdateRoadmapPort;
import com.astra.roadmap.domain.Roadmap;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class RemoveStepResourceService implements RemoveStepResourceUseCase {

    private final LoadRoadmapPort loadRoadmapPort;
    private final UpdateRoadmapPort updateRoadmapPort;

    RemoveStepResourceService(LoadRoadmapPort loadRoadmapPort, UpdateRoadmapPort updateRoadmapPort) {
        this.loadRoadmapPort = loadRoadmapPort;
        this.updateRoadmapPort = updateRoadmapPort;
    }

    @Override
    @Transactional
    public void remove(UUID userId, UUID stepId, UUID resourceId) {
        Roadmap roadmap = loadRoadmapPort.loadOwnedByStep(stepId, userId)
                .orElseThrow(() -> new NotFoundException("Step not found"));
        roadmap.removeResource(stepId, resourceId);
        updateRoadmapPort.update(roadmap);
    }
}
