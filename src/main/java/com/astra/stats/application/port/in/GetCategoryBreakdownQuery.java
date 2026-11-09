package com.astra.stats.application.port.in;

import com.astra.stats.domain.CategoryMinutes;
import java.util.List;
import java.util.UUID;

public interface GetCategoryBreakdownQuery {

    List<CategoryMinutes> breakdown(UUID userId, int days);
}
