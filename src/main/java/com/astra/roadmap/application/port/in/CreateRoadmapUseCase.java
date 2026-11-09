package com.astra.roadmap.application.port.in;

public interface CreateRoadmapUseCase {

    RoadmapResponse create(CreateRoadmapCommand command);
}
