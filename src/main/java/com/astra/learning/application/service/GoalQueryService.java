package com.astra.learning.application.service;

import com.astra.learning.application.port.in.ExportGoalsQuery;
import com.astra.learning.application.port.in.GoalResponse;
import com.astra.learning.application.port.in.ListGoalsQuery;
import com.astra.learning.application.port.out.LoadGoalsPort;
import com.astra.shared.ExportRow;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class GoalQueryService implements ListGoalsQuery, ExportGoalsQuery {

    private final LoadGoalsPort loadGoalsPort;

    GoalQueryService(LoadGoalsPort loadGoalsPort) {
        this.loadGoalsPort = loadGoalsPort;
    }

    @Override
    public List<GoalResponse> list(UUID userId) {
        return loadGoalsPort.loadByUser(userId).stream().map(GoalResponse::from).toList();
    }

    @Override
    public List<Map<String, Object>> exportGoals(UUID userId) {
        return loadGoalsPort.loadByUser(userId).stream()
                .map(goal -> ExportRow.of("type", goal.type(), "targetHours", goal.targetHours()))
                .toList();
    }
}
