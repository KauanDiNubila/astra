package com.astra.tracking.category.application.service;

import com.astra.tracking.category.application.port.in.CategoryResponse;
import com.astra.tracking.category.application.port.in.CreateCategoryCommand;
import com.astra.tracking.category.application.port.in.CreateCategoryUseCase;
import com.astra.tracking.category.application.port.out.CheckCategoryNameTakenPort;
import com.astra.tracking.category.application.port.out.CreateCategoryPort;
import com.astra.tracking.category.domain.Category;
import com.astra.tracking.category.domain.CategoryNameTakenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateCategoryService implements CreateCategoryUseCase {

    private final CheckCategoryNameTakenPort checkCategoryNameTakenPort;
    private final CreateCategoryPort createCategoryPort;

    CreateCategoryService(CheckCategoryNameTakenPort checkCategoryNameTakenPort,
                          CreateCategoryPort createCategoryPort) {
        this.checkCategoryNameTakenPort = checkCategoryNameTakenPort;
        this.createCategoryPort = createCategoryPort;
    }

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryCommand command) {
        if (checkCategoryNameTakenPort.isNameTaken(command.userId(), command.name())) {
            throw new CategoryNameTakenException(command.name());
        }
        Category category = new Category(command.userId(), command.name(), command.color());
        return CategoryResponse.from(createCategoryPort.create(category));
    }
}
