package com.astra.tracking.category.application.port.in;

import java.util.UUID;

public interface CreateDefaultCategoriesUseCase {

    void createDefaults(UUID userId);
}
