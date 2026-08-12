package com.astra.tracking.category;

import com.astra.shared.CurrentUserProvider;
import com.astra.shared.event.UserRegisteredEvent;
import com.astra.shared.exception.ConflictException;
import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.category.dto.CategoryResponse;
import com.astra.tracking.category.dto.CreateCategoryRequest;
import com.astra.tracking.session.SessionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
public class CategoryService {

    private static final List<String> DEFAULT_CATEGORY_NAMES = List.of("Trabalho", "Estudo");

    private final CategoryRepository categoryRepository;
    private final SessionRepository sessionRepository;
    private final CurrentUserProvider currentUserProvider;

    public CategoryService(CategoryRepository categoryRepository, SessionRepository sessionRepository,
                           CurrentUserProvider currentUserProvider) {
        this.categoryRepository = categoryRepository;
        this.sessionRepository = sessionRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onUserRegistered(UserRegisteredEvent event) {
        for (String name : DEFAULT_CATEGORY_NAMES) {
            categoryRepository.save(new Category(event.userId(), name, null));
        }
    }

    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        Category category = new Category(userId, request.name(), request.color());
        Category saved = categoryRepository.save(category);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listForCurrentUser() {
        UUID userId = currentUserProvider.currentUserId();
        return categoryRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = currentUserProvider.currentUserId();
        Category category = categoryRepository.findById(id)
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (sessionRepository.existsByCategoryId(id)) {
            throw new ConflictException("Category has sessions and cannot be deleted");
        }
        categoryRepository.delete(category);
    }

    private CategoryResponse toDto(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getColor());
    }
}
