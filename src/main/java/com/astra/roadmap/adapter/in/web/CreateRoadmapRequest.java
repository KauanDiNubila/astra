package com.astra.roadmap.adapter.in.web;

import com.astra.roadmap.application.port.in.CreateRoadmapCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record CreateRoadmapRequest(
        @NotBlank @Size(max = CreateRoadmapCommand.TITLE_MAX_LENGTH) String title,
        @Size(max = CreateRoadmapCommand.SOURCE_MAX_LENGTH) String source
) {

    CreateRoadmapCommand toCommand(UUID userId) {
        return new CreateRoadmapCommand(userId, title, source);
    }
}
