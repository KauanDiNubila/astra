package com.astra.roadmap.application.service;

import com.astra.roadmap.application.port.in.CreateRoadmapCommand;
import com.astra.roadmap.application.port.in.CreateRoadmapUseCase;
import com.astra.roadmap.application.port.in.RoadmapResponse;
import com.astra.roadmap.application.port.out.CreateRoadmapPort;
import com.astra.roadmap.domain.Roadmap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateRoadmapService implements CreateRoadmapUseCase {

    private final CreateRoadmapPort createRoadmapPort;

    CreateRoadmapService(CreateRoadmapPort createRoadmapPort) {
        this.createRoadmapPort = createRoadmapPort;
    }

    @Override
    @Transactional
    public RoadmapResponse create(CreateRoadmapCommand command) {
        Roadmap roadmap = Roadmap.create(command.userId(), command.title(), command.source());
        createRoadmapPort.create(roadmap);
        return RoadmapResponse.from(roadmap.outline(), roadmap.outline().progress(Map.of()));
    }
}
