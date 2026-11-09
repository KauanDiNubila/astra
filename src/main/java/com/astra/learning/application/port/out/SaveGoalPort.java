package com.astra.learning.application.port.out;

import com.astra.learning.domain.Goal;

public interface SaveGoalPort {

    void save(Goal goal);
}
