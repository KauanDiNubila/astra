package com.astra.learning.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListGoalsQuery {

    List<GoalResponse> list(UUID userId);
}
