package com.astra.roadmap.adapter.in.web;

import com.astra.roadmap.application.port.in.AddStepResourceCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record AddResourceRequest(
        @NotBlank @Size(max = AddStepResourceCommand.LABEL_MAX_LENGTH) String label,
        @NotBlank @Size(max = AddStepResourceCommand.URL_MAX_LENGTH) String url
) {

    AddStepResourceCommand toCommand(UUID userId, UUID stepId) {
        return new AddStepResourceCommand(userId, stepId, label, url);
    }
}
