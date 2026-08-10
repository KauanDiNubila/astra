package com.astra.stats.dto;

import java.util.UUID;

public record RankingEntry(
        int position,
        UUID userId,
        String name,
        long minutes
) {
}
