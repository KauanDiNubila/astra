package com.astra.tracking.session.application.port.in;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface GetFocusedMinutesQuery {

    long total(UUID userId);

    long since(UUID userId, OffsetDateTime start);

    List<DailyMinutes> dailySince(UUID userId, OffsetDateTime start);

    List<CategoryMinutes> byCategorySince(UUID userId, OffsetDateTime start);

    record DailyMinutes(LocalDate day, long minutes) {
    }

    record CategoryMinutes(UUID categoryId, long minutes) {
    }
}
