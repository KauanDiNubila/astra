package com.astra.tracking.category.application.port.in;

public interface CreateCategoryUseCase {

    CategoryResponse create(CreateCategoryCommand command);
}
