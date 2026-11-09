package com.astra.stats.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.astra.stats.application.port.in.DashboardResponse;
import com.astra.stats.application.port.in.GoalProgress;
import com.astra.stats.application.port.in.RankingEntry;
import com.astra.stats.application.port.in.RankingScope;
import com.astra.stats.application.port.out.LoadFocusedMinutesPort;
import com.astra.stats.application.port.out.LoadFriendIdsPort;
import com.astra.stats.application.port.out.LoadGoalsPort;
import com.astra.stats.application.port.out.LoadMinutesByUserPort;
import com.astra.stats.application.port.out.LoadMinutesByUserPort.UserMinutes;
import com.astra.stats.application.port.out.LoadRankedUsersPort;
import com.astra.stats.application.port.out.LoadRankedUsersPort.RankedUser;
import com.astra.stats.domain.DayMinutes;
import com.astra.stats.domain.Goal;
import com.astra.stats.domain.GoalType;
import com.astra.stats.domain.RankingPeriod;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StatsServicesTest {

    private static final UUID ANA = UUID.randomUUID();
    private static final UUID BIA = UUID.randomUUID();
    private static final UUID CAIO = UUID.randomUUID();

    private final LoadFocusedMinutesPort focusedMinutesPort = mock(LoadFocusedMinutesPort.class);
    private final LoadGoalsPort goalsPort = mock(LoadGoalsPort.class);
    private final LoadMinutesByUserPort minutesByUserPort = mock(LoadMinutesByUserPort.class);
    private final LoadFriendIdsPort friendIdsPort = mock(LoadFriendIdsPort.class);
    private final LoadRankedUsersPort rankedUsersPort = mock(LoadRankedUsersPort.class);

    private final StatsQueryService statsService = new StatsQueryService(focusedMinutesPort, goalsPort);
    private final RankingQueryService rankingService =
            new RankingQueryService(minutesByUserPort, friendIdsPort, rankedUsersPort);

    @Test
    void dashboardJuntaOHistoricoOTotalEAsMetas() {
        LocalDate today = StatsCalendar.today();
        when(focusedMinutesPort.dailySince(eq(ANA), any())).thenReturn(List.of(
                new DayMinutes(today, 120), new DayMinutes(today.minusDays(1), 30)));
        when(focusedMinutesPort.totalMinutes(ANA)).thenReturn(500L);
        when(goalsPort.goalsOf(ANA)).thenReturn(List.of(new Goal(GoalType.DAILY, 2)));

        DashboardResponse dashboard = statsService.dashboard(ANA);

        assertThat(dashboard.todayMinutes()).isEqualTo(120);
        assertThat(dashboard.totalMinutes()).isEqualTo(500);
        assertThat(dashboard.currentStreak()).isEqualTo(2);
        assertThat(dashboard.longestStreak()).isEqualTo(2);
        assertThat(dashboard.last30DaysMinutes()).isEqualTo(150);
        assertThat(dashboard.goals()).containsExactly(new GoalProgress(GoalType.DAILY, 2, 2.0, true));
    }

    @Test
    void heatmapEPorCategoriaComecamNoInicioDoDia() {
        LocalDate today = StatsCalendar.today();

        statsService.heatmap(ANA);
        statsService.breakdown(ANA, 30);

        verify(focusedMinutesPort).dailySince(ANA, StatsCalendar.startOf(today.minusDays(365)));
        verify(focusedMinutesPort).byCategorySince(ANA, StatsCalendar.startOf(today.minusDays(29)));
    }

    @Test
    void rankingNumeraNaOrdemEUsaNomeVazioParaQuemSumiu() {
        when(minutesByUserPort.since(StatsCalendar.startOf(StatsCalendar.today()))).thenReturn(List.of(
                new UserMinutes(BIA, 90), new UserMinutes(CAIO, 40)));
        when(rankedUsersPort.loadByIds(List.of(BIA, CAIO))).thenReturn(Map.of(BIA, new RankedUser("Bia", true)));

        List<RankingEntry> ranking = rankingService.ranking(ANA, RankingPeriod.DAILY, RankingScope.GLOBAL);

        assertThat(ranking).containsExactly(
                new RankingEntry(1, BIA, "Bia", 90, true),
                new RankingEntry(2, CAIO, "", 40, false));
        verify(friendIdsPort, never()).friendIdsOf(any());
    }

    @Test
    void rankingDeAmigosIncluiAPropriaPessoa() {
        when(friendIdsPort.friendIdsOf(ANA)).thenReturn(List.of(BIA));
        when(rankedUsersPort.loadByIds(anyCollection())).thenReturn(Map.of());

        rankingService.ranking(ANA, RankingPeriod.MONTHLY, RankingScope.FRIENDS);

        verify(minutesByUserPort).sinceForUsers(
                StatsCalendar.startOf(StatsCalendar.today().withDayOfMonth(1)), Set.of(BIA, ANA));
        verify(minutesByUserPort, never()).since(any());
    }
}
