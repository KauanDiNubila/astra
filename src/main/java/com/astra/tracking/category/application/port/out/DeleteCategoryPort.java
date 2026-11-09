package com.astra.tracking.category.application.port.out;

import com.astra.tracking.category.domain.Category;

public interface DeleteCategoryPort {

    void delete(Category category);
}
