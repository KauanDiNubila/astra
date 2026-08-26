package com.astra.stats;

import com.astra.stats.dto.DashboardResponse;
import com.astra.tracking.session.CategoryMinutes;
import com.astra.tracking.session.DailyMinutes;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("/dashboard/by-category")
    public List<CategoryMinutes> categoryBreakdown(@RequestParam(defaultValue = "30") int days) {
        return statsService.categoryBreakdown(days);
    }
}
