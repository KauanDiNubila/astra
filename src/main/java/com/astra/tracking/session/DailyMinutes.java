package com.astra.tracking.session;

import java.time.LocalDate;

public record DailyMinutes(LocalDate day, long minutes) {
}
