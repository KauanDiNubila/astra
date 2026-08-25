package com.astra.roadmap.dto;

import java.util.UUID;

public record ResourceResponse(
        UUID id,
        String label,
        String url,
        int position
) {
}
