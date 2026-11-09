package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.AddStepResourceCommand;
import com.astra.roadmap.application.port.in.AddStepResourceUseCase;
import com.astra.roadmap.application.port.in.ResourceResponse;
import com.astra.roadmap.application.port.out.LoadRoadmapPort;
import com.astra.roadmap.application.port.out.UpdateRoadmapPort;
import com.astra.roadmap.domain.Roadmap;
import com.astra.roadmap.domain.StepResource;
import com.astra.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AddStepResourceService implements AddStepResourceUseCase {

    private final LoadRoadmapPort loadRoadmapPort;
    private final UpdateRoadmapPort updateRoadmapPort;

    AddStepResourceService(LoadRoadmapPort loadRoadmapPort, UpdateRoadmapPort updateRoadmapPort) {
        this.loadRoadmapPort = loadRoadmapPort;
        this.updateRoadmapPort = updateRoadmapPort;
    }

    @Override
    @Transactional
    public ResourceResponse add(AddStepResourceCommand command) {
        Roadmap roadmap = loadRoadmapPort.loadOwnedByStep(command.stepId(), command.userId())
                .orElseThrow(() -> new NotFoundException("Step not found"));
        StepResource resource = roadmap.addResource(command.stepId(), command.label(), command.url());
        updateRoadmapPort.update(roadmap);
        return ResourceResponse.from(resource);
    }
}
