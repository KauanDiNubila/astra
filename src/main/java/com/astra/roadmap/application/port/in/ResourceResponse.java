package com.astra.roadmap.application.port.in;

import com.astra.roadmap.domain.StepResource;
import java.util.UUID;

public record ResourceResponse(UUID id, String label, String url, int position) {

    public static ResourceResponse from(StepResource resource) {
        return new ResourceResponse(resource.getId(), resource.getLabel(), resource.getUrl(),
                resource.getPosition());
    }
}
