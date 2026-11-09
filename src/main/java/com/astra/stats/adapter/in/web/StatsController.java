package com.astra.stats.adapter.in.web;

import com.astra.shared.CurrentUserProvider;
import com.astra.stats.application.port.in.DashboardResponse;
import com.astra.stats.application.port.in.GetCategoryBreakdownQuery;
import com.astra.stats.application.port.in.GetDashboardQuery;
import com.astra.stats.application.port.in.GetHeatmapQuery;
import com.astra.stats.domain.CategoryMinutes;
import com.astra.stats.domain.DayMinutes;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
class StatsController {

    private final GetDashboardQuery getDashboardQuery;
    private final GetHeatmapQuery getHeatmapQuery;
    private final GetCategoryBreakdownQuery getCategoryBreakdownQuery;
    private final CurrentUserProvider currentUserProvider;

    StatsController(GetDashboardQuery getDashboardQuery, GetHeatmapQuery getHeatmapQuery,
                    GetCategoryBreakdownQuery getCategoryBreakdownQuery, CurrentUserProvider currentUserProvider) {
        this.getDashboardQuery = getDashboardQuery;
        this.getHeatmapQuery = getHeatmapQuery;
        this.getCategoryBreakdownQuery = getCategoryBreakdownQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/dashboard")
    DashboardResponse dashboard() {
        return getDashboardQuery.dashboard(currentUserProvider.currentUserId());
    }

    @GetMapping("/heatmap")
    List<DayMinutes> heatmap() {
        return getHeatmapQuery.heatmap(currentUserProvider.currentUserId());
    }

    @GetMapping("/dashboard/by-category")
    List<CategoryMinutes> categoryBreakdown(@RequestParam(defaultValue = "30") int days) {
        return getCategoryBreakdownQuery.breakdown(currentUserProvider.currentUserId(), days);
    }
}
