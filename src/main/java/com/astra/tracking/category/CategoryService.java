package com.astra.tracking.category;

import com.astra.shared.CurrentUserProvider;
import com.astra.tracking.category.dto.CategoryResponse;
import com.astra.tracking.category.dto.CreateCategoryRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;

    public CategoryService(CategoryRepository categoryRepository,
                           CurrentUserProvider currentUserProvider) {
        this.categoryRepository = categoryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        Category category = new Category(userId, request.name(), request.color());
        Category saved = categoryRepository.save(category);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listForCurrentUser() {
        UUID userId = currentUserProvider.currentUserId();
        return categoryRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getColor());
    }
}
