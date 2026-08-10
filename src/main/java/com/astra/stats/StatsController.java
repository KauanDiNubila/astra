package com.astra.stats;

import com.astra.stats.dto.DashboardResponse;
import com.astra.tracking.session.DailyMinutes;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard() {
        return statsService.dashboard();
    }

    @GetMapping("/heatmap")
    public List<DailyMinutes> heatmap() {
        return statsService.heatmap();
    }
}
