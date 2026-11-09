package com.astra.tracking.category.application.service;

import com.astra.tracking.category.application.port.in.CreateDefaultCategoriesUseCase;
import com.astra.tracking.category.application.port.out.CreateCategoryPort;
import com.astra.tracking.category.domain.Category;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class CreateDefaultCategoriesService implements CreateDefaultCategoriesUseCase {

    static final List<String> DEFAULT_NAMES = List.of("Trabalho", "Estudo");

    private final CreateCategoryPort createCategoryPort;

    CreateDefaultCategoriesService(CreateCategoryPort createCategoryPort) {
        this.createCategoryPort = createCategoryPort;
    }

    @Override
    @Transactional
    public void createDefaults(UUID userId) {
        for (String name : DEFAULT_NAMES) {
            createCategoryPort.create(new Category(userId, name, null));
        }
    }
}
