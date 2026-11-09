package com.astra.stats.application.port.in;

import com.astra.stats.domain.DayMinutes;
import java.util.List;
import java.util.UUID;

public interface GetHeatmapQuery {

    List<DayMinutes> heatmap(UUID userId);
}
