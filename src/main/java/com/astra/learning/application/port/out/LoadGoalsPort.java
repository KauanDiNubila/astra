package com.astra.learning.application.port.out;

import com.astra.learning.domain.Goal;
import java.util.List;
import java.util.UUID;

public interface LoadGoalsPort {

    List<Goal> loadByUser(UUID userId);
}
