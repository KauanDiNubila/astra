package com.astra.github.domain;

import java.time.LocalDate;

public record DailyContribution(LocalDate date, int count) {
}
