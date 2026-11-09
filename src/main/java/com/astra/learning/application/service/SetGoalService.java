package com.astra.learning.application.service;

import com.astra.learning.application.port.in.GoalResponse;
import com.astra.learning.application.port.in.SetGoalCommand;
import com.astra.learning.application.port.in.SetGoalUseCase;
import com.astra.learning.application.port.out.SaveGoalPort;
import com.astra.learning.domain.Goal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SetGoalService implements SetGoalUseCase {

    private final SaveGoalPort saveGoalPort;

    SetGoalService(SaveGoalPort saveGoalPort) {
        this.saveGoalPort = saveGoalPort;
    }

    @Override
    @Transactional
    public GoalResponse set(SetGoalCommand command) {
        Goal goal = new Goal(command.userId(), command.type(), command.targetHours());
        saveGoalPort.save(goal);
        return GoalResponse.from(goal);
    }
}
