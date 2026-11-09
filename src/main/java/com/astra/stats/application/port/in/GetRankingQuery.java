package com.astra.stats.application.port.in;

import com.astra.stats.domain.RankingPeriod;
import java.util.List;
import java.util.UUID;

public interface GetRankingQuery {

    List<RankingEntry> ranking(UUID userId, RankingPeriod period, RankingScope scope);
}
