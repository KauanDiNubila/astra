package com.astra.stats.adapter.in.web;

import com.astra.shared.CurrentUserProvider;
import com.astra.stats.application.port.in.GetRankingQuery;
import com.astra.stats.application.port.in.RankingEntry;
import com.astra.stats.application.port.in.RankingScope;
import com.astra.stats.domain.RankingPeriod;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ranking")
class RankingController {

    private final GetRankingQuery getRankingQuery;
    private final CurrentUserProvider currentUserProvider;

    RankingController(GetRankingQuery getRankingQuery, CurrentUserProvider currentUserProvider) {
        this.getRankingQuery = getRankingQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping
    List<RankingEntry> ranking(
            @RequestParam(defaultValue = "DAILY") RankingPeriod period,
            @RequestParam(defaultValue = "GLOBAL") RankingScope scope) {
        return getRankingQuery.ranking(currentUserProvider.currentUserId(), period, scope);
    }
}
