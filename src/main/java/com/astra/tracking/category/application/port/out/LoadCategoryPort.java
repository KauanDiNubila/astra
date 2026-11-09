package com.astra.tracking.category.application.port.out;

import com.astra.tracking.category.domain.Category;
import java.util.Optional;
import java.util.UUID;

public interface LoadCategoryPort {

    Optional<Category> load(UUID categoryId);
}
