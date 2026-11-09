package com.astra.roadmap.application.port.out;

import com.astra.roadmap.domain.Roadmap;

public interface CreateRoadmapPort {

    void create(Roadmap roadmap);
}
