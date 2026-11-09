package com.astra.tracking.category.adapter.in.web;

import com.astra.shared.CurrentUserProvider;
import com.astra.tracking.category.application.port.in.CategoryResponse;
import com.astra.tracking.category.application.port.in.CreateCategoryUseCase;
import com.astra.tracking.category.application.port.in.DeleteCategoryUseCase;
import com.astra.tracking.category.application.port.in.ListCategoriesQuery;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/categories")
class CategoryController {

    private final CreateCategoryUseCase createCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final ListCategoriesQuery listCategoriesQuery;
    private final CurrentUserProvider currentUserProvider;

    CategoryController(CreateCategoryUseCase createCategoryUseCase, DeleteCategoryUseCase deleteCategoryUseCase,
                       ListCategoriesQuery listCategoriesQuery, CurrentUserProvider currentUserProvider) {
        this.createCategoryUseCase = createCategoryUseCase;
        this.deleteCategoryUseCase = deleteCategoryUseCase;
        this.listCategoriesQuery = listCategoriesQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CategoryResponse create(@Valid @RequestBody CreateCategoryRequest request) {
        return createCategoryUseCase.create(request.toCommand(currentUserProvider.currentUserId()));
    }

    @GetMapping
    List<CategoryResponse> list() {
        return listCategoriesQuery.list(currentUserProvider.currentUserId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        deleteCategoryUseCase.delete(currentUserProvider.currentUserId(), id);
    }
}
