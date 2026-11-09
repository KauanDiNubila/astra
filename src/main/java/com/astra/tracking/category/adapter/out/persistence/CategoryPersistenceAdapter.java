package com.astra.tracking.category.adapter.out.persistence;

import com.astra.tracking.category.application.port.out.CheckCategoryNameTakenPort;
import com.astra.tracking.category.application.port.out.CreateCategoryPort;
import com.astra.tracking.category.application.port.out.DeleteCategoryPort;
import com.astra.tracking.category.application.port.out.ListCategoriesPort;
import com.astra.tracking.category.application.port.out.LoadCategoryPort;
import com.astra.tracking.category.domain.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CategoryPersistenceAdapter implements CreateCategoryPort, LoadCategoryPort, ListCategoriesPort,
        CheckCategoryNameTakenPort, DeleteCategoryPort {

    private final SpringDataCategoryRepository repository;

    CategoryPersistenceAdapter(SpringDataCategoryRepository repository) {
        this.repository = repository;
    }

    @Override
    public Category create(Category category) {
        return repository.save(category);
    }

    @Override
    public Optional<Category> load(UUID categoryId) {
        return repository.findById(categoryId);
    }

    @Override
    public List<Category> listByUser(UUID userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public boolean isNameTaken(UUID userId, String name) {
        return repository.existsByUserIdAndName(userId, name);
    }

    @Override
    public void delete(Category category) {
        repository.delete(category);
    }
}
