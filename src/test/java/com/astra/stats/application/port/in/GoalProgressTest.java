package com.astra.stats.application.port.in;

import static org.assertj.core.api.Assertions.assertThat;

import com.astra.stats.domain.Goal;
import com.astra.stats.domain.GoalType;
import org.junit.jupiter.api.Test;

class GoalProgressTest {

    @Test
    void montaOProgressoAPartirDaMeta() {
        assertThat(GoalProgress.of(new Goal(GoalType.WEEKLY, 5), 30, 300))
                .isEqualTo(new GoalProgress(GoalType.WEEKLY, 5, 5.0, true));
        assertThat(GoalProgress.of(new Goal(GoalType.DAILY, 1), 30, 300))
                .isEqualTo(new GoalProgress(GoalType.DAILY, 1, 0.5, false));
    }
}
