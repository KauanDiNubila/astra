package com.astra.tracking.category.application.port.out;

import com.astra.tracking.category.domain.Category;
import java.util.List;
import java.util.UUID;

public interface ListCategoriesPort {

    List<Category> listByUser(UUID userId);
}
