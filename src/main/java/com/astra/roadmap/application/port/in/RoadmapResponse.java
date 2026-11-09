package com.astra.roadmap.application.port.in;

import com.astra.roadmap.domain.RoadmapOutline;
import com.astra.roadmap.domain.RoadmapProgress;
import java.util.UUID;

public record RoadmapResponse(UUID id, String title, String source, boolean predefined, RoadmapProgress progress) {

    public static RoadmapResponse from(RoadmapOutline roadmap, RoadmapProgress progress) {
        return new RoadmapResponse(roadmap.id(), roadmap.title(), roadmap.source(), roadmap.isPredefined(),
                progress);
    }
}
