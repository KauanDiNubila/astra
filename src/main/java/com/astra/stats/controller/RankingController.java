package com.astra.stats.controller;

import com.astra.stats.dto.RankingEntry;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.astra.stats.dto.RankingPeriod;
import com.astra.stats.dto.RankingScope;
import com.astra.stats.service.RankingService;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public List<RankingEntry> ranking(
            @RequestParam(defaultValue = "DAILY") RankingPeriod period,
            @RequestParam(defaultValue = "GLOBAL") RankingScope scope) {
        return rankingService.ranking(period, scope);
    }
}
