package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.SetStepStatusUseCase;
import com.astra.roadmap.application.port.in.StepResponse;
import com.astra.roadmap.application.port.out.LoadStepPort;
import com.astra.roadmap.application.port.out.SaveStepStatusPort;
import com.astra.roadmap.domain.LocatedStep;
import com.astra.roadmap.domain.StepStatus;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SetStepStatusService implements SetStepStatusUseCase {

    private final LoadStepPort loadStepPort;
    private final SaveStepStatusPort saveStepStatusPort;

    SetStepStatusService(LoadStepPort loadStepPort, SaveStepStatusPort saveStepStatusPort) {
        this.loadStepPort = loadStepPort;
        this.saveStepStatusPort = saveStepStatusPort;
    }

    @Override
    @Transactional
    public StepResponse setStatus(UUID userId, UUID roadmapId, UUID stepId, StepStatus status) {
        LocatedStep located = loadStepPort.loadStep(stepId)
                .filter(step -> step.isIn(roadmapId))
                .orElseThrow(() -> new NotFoundException("Step not found"));
        if (!located.isAccessibleBy(userId)) {
            throw new NotFoundException("Roadmap not found");
        }
        saveStepStatusPort.save(userId, stepId, status);
        return StepResponse.from(located.step(), status);
    }
}
