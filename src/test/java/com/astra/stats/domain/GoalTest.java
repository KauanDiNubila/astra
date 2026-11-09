package com.astra.stats.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GoalTest {

    @Test
    void metaDiariaUsaOsMinutosDeHojeESemanalOsDaSemana() {
        assertThat(new Goal(GoalType.DAILY, 2).achievedHours(90, 600)).isEqualTo(1.5);
        assertThat(new Goal(GoalType.WEEKLY, 10).achievedHours(90, 600)).isEqualTo(10.0);
    }

    @Test
    void atingeQuandoChegaNaMeta() {
        Goal goal = new Goal(GoalType.DAILY, 2);

        assertThat(goal.isReachedWith(2.0)).isTrue();
        assertThat(goal.isReachedWith(1.99)).isFalse();
    }
}
