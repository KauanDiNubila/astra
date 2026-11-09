package com.astra.roadmap.adapter.in.web;

import com.astra.roadmap.application.port.in.AddStepCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record CreateStepRequest(
        @NotBlank @Size(max = AddStepCommand.TITLE_MAX_LENGTH) String title,
        @NotNull @PositiveOrZero Integer position,
        UUID parentStepId,
        @Size(max = AddStepCommand.DESCRIPTION_MAX_LENGTH) String description
) {

    AddStepCommand toCommand(UUID userId, UUID roadmapId) {
        return new AddStepCommand(userId, roadmapId, title, position, parentStepId, description);
    }
}
