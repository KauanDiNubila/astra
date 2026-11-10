package com.astra.github.application.port.out;

import com.astra.github.domain.ActivityPeriod;
import com.astra.github.domain.PeriodSummary;
import com.astra.github.domain.YearSnapshot;
import java.time.LocalDate;

public interface LoadContributionsPort {

    YearSnapshot year(String accessToken, String login, LocalDate from, LocalDate to);

    PeriodSummary period(String accessToken, String login, ActivityPeriod period, LocalDate from, LocalDate to);
}
