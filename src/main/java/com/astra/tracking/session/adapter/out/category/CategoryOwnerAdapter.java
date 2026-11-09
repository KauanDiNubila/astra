package com.astra.tracking.session.adapter.out.category;

import com.astra.tracking.category.repository.CategoryRepository;
import com.astra.tracking.session.application.port.out.CheckCategoryOwnerPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CategoryOwnerAdapter implements CheckCategoryOwnerPort {

    private final CategoryRepository categoryRepository;

    CategoryOwnerAdapter(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public boolean isOwner(UUID categoryId, UUID userId) {
        return categoryRepository.findById(categoryId)
                .filter(category -> category.getUserId().equals(userId))
                .isPresent();
    }
}
