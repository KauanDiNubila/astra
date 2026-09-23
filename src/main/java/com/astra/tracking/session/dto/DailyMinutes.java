package com.astra.tracking.session.dto;

import java.time.LocalDate;

public record DailyMinutes(LocalDate day, long minutes) {
}
