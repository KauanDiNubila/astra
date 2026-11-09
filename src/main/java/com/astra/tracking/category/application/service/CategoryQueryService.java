package com.astra.tracking.category.application.service;

import com.astra.tracking.category.application.port.in.CategoryResponse;
import com.astra.tracking.category.application.port.in.CheckCategoryOwnerQuery;
import com.astra.tracking.category.application.port.in.ListCategoriesQuery;
import com.astra.tracking.category.application.port.out.ListCategoriesPort;
import com.astra.tracking.category.application.port.out.LoadCategoryPort;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class CategoryQueryService implements ListCategoriesQuery, CheckCategoryOwnerQuery {

    private final ListCategoriesPort listCategoriesPort;
    private final LoadCategoryPort loadCategoryPort;

    CategoryQueryService(ListCategoriesPort listCategoriesPort, LoadCategoryPort loadCategoryPort) {
        this.listCategoriesPort = listCategoriesPort;
        this.loadCategoryPort = loadCategoryPort;
    }

    @Override
    public List<CategoryResponse> list(UUID userId) {
        return listCategoriesPort.listByUser(userId).stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Override
    public boolean isOwner(UUID categoryId, UUID userId) {
        return loadCategoryPort.load(categoryId)
                .filter(category -> category.belongsTo(userId))
                .isPresent();
    }
}
