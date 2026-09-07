package com.astra.github.dto;

import java.time.LocalDate;

public record GitHubDailyPoint(LocalDate date, int contributionCount) {
}
