package com.astra.stats.application.port.in;

import java.util.UUID;

public record RankingEntry(
        int position,
        UUID userId,
        String name,
        long minutes,
        boolean admin
) {
}
