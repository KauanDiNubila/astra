package com.astra.stats.application.port.out;

import com.astra.stats.domain.CategoryMinutes;
import com.astra.stats.domain.DayMinutes;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface LoadFocusedMinutesPort {

    long totalMinutes(UUID userId);

    List<DayMinutes> dailySince(UUID userId, OffsetDateTime start);

    List<CategoryMinutes> byCategorySince(UUID userId, OffsetDateTime start);
}
