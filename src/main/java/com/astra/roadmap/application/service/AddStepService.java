package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.AddStepCommand;
import com.astra.roadmap.application.port.in.AddStepUseCase;
import com.astra.roadmap.application.port.in.StepResponse;
import com.astra.roadmap.application.port.out.LoadRoadmapPort;
import com.astra.roadmap.application.port.out.UpdateRoadmapPort;
import com.astra.roadmap.domain.Roadmap;
import com.astra.roadmap.domain.RoadmapStep;
import com.astra.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AddStepService implements AddStepUseCase {

    private final LoadRoadmapPort loadRoadmapPort;
    private final UpdateRoadmapPort updateRoadmapPort;

    AddStepService(LoadRoadmapPort loadRoadmapPort, UpdateRoadmapPort updateRoadmapPort) {
        this.loadRoadmapPort = loadRoadmapPort;
        this.updateRoadmapPort = updateRoadmapPort;
    }

    @Override
    @Transactional
    public StepResponse addStep(AddStepCommand command) {
        Roadmap roadmap = loadRoadmapPort.load(command.roadmapId())
                .filter(r -> r.isOwnedBy(command.userId()))
                .orElseThrow(() -> new NotFoundException("Roadmap not found"));
        RoadmapStep step = roadmap.addStep(command.title(), command.position(), command.parentStepId(),
                command.description());
        updateRoadmapPort.update(roadmap);
        return StepResponse.from(step, null);
    }
}
