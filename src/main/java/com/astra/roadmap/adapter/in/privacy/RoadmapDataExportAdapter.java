package com.astra.roadmap.adapter.in.privacy;

import com.astra.roadmap.application.port.in.ExportRoadmapsQuery;
import com.astra.roadmap.application.port.in.ExportCoursePinsQuery;
import com.astra.privacy.application.port.out.ExportUserDataPort;
import com.astra.shared.ExportRow;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(5)
class RoadmapDataExportAdapter implements ExportUserDataPort {

    private final ExportRoadmapsQuery exportRoadmapsQuery;
    private final ExportCoursePinsQuery exportCoursePinsQuery;

    RoadmapDataExportAdapter(ExportRoadmapsQuery exportRoadmapsQuery, ExportCoursePinsQuery exportCoursePinsQuery) {
        this.exportRoadmapsQuery = exportRoadmapsQuery;
        this.exportCoursePinsQuery = exportCoursePinsQuery;
    }

    @Override
    public Map<String, Object> export(UUID userId) {
        return ExportRow.of("roadmaps", exportRoadmapsQuery.exportRoadmaps(userId),
                "coursePins", exportCoursePinsQuery.exportPins(userId));
    }
}
