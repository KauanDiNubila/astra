package com.astra.learning.adapter.in.privacy;

import com.astra.learning.application.port.in.ExportCoursesQuery;
import com.astra.learning.application.port.in.ExportGoalsQuery;
import com.astra.privacy.application.port.out.ExportUserDataPort;
import com.astra.shared.ExportRow;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
class LearningDataExportAdapter implements ExportUserDataPort {

    private final ExportCoursesQuery exportCoursesQuery;
    private final ExportGoalsQuery exportGoalsQuery;

    LearningDataExportAdapter(ExportCoursesQuery exportCoursesQuery, ExportGoalsQuery exportGoalsQuery) {
        this.exportCoursesQuery = exportCoursesQuery;
        this.exportGoalsQuery = exportGoalsQuery;
    }

    @Override
    public Map<String, Object> export(UUID userId) {
        return ExportRow.of("courses", exportCoursesQuery.exportCourses(userId),
                "goals", exportGoalsQuery.exportGoals(userId));
    }
}
