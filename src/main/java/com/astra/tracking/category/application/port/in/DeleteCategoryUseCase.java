package com.astra.tracking.category.application.port.in;

import java.util.UUID;

public interface DeleteCategoryUseCase {

    void delete(UUID userId, UUID categoryId);
}
