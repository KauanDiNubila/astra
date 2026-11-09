package com.astra.tracking.category.adapter.in.web;

import com.astra.tracking.category.application.port.in.CreateCategoryCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record CreateCategoryRequest(
        @NotBlank @Size(max = CreateCategoryCommand.NAME_MAX_LENGTH) String name,
        @Size(max = CreateCategoryCommand.COLOR_MAX_LENGTH) String color
) {

    CreateCategoryCommand toCommand(UUID userId) {
        return new CreateCategoryCommand(userId, name, color);
    }
}
