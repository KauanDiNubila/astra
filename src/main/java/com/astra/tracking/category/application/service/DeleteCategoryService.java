package com.astra.tracking.category.application.service;

import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.category.application.port.in.DeleteCategoryUseCase;
import com.astra.tracking.category.application.port.out.CheckCategoryInUsePort;
import com.astra.tracking.category.application.port.out.DeleteCategoryPort;
import com.astra.tracking.category.application.port.out.LoadCategoryPort;
import com.astra.tracking.category.domain.Category;
import com.astra.tracking.category.domain.CategoryInUseException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DeleteCategoryService implements DeleteCategoryUseCase {

    private final LoadCategoryPort loadCategoryPort;
    private final CheckCategoryInUsePort checkCategoryInUsePort;
    private final DeleteCategoryPort deleteCategoryPort;

    DeleteCategoryService(LoadCategoryPort loadCategoryPort, CheckCategoryInUsePort checkCategoryInUsePort,
                          DeleteCategoryPort deleteCategoryPort) {
        this.loadCategoryPort = loadCategoryPort;
        this.checkCategoryInUsePort = checkCategoryInUsePort;
        this.deleteCategoryPort = deleteCategoryPort;
    }

    @Override
    @Transactional
    public void delete(UUID userId, UUID categoryId) {
        Category category = loadCategoryPort.load(categoryId)
                .filter(c -> c.belongsTo(userId))
                .orElseThrow(() -> new NotFoundException("Category not found"));
        if (checkCategoryInUsePort.isInUse(categoryId)) {
            throw new CategoryInUseException();
        }
        deleteCategoryPort.delete(category);
    }
}
