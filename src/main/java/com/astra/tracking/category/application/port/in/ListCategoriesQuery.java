package com.astra.tracking.category.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListCategoriesQuery {

    List<CategoryResponse> list(UUID userId);
}
