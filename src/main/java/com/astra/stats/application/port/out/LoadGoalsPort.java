package com.astra.stats.application.port.out;

import com.astra.stats.domain.Goal;
import java.util.List;
import java.util.UUID;

public interface LoadGoalsPort {

    List<Goal> goalsOf(UUID userId);
}
