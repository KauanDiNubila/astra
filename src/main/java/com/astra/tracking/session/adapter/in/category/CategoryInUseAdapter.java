package com.astra.tracking.session.adapter.in.category;

import com.astra.tracking.category.application.port.out.CheckCategoryInUsePort;
import com.astra.tracking.session.application.port.in.HasSessionsInCategoryQuery;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CategoryInUseAdapter implements CheckCategoryInUsePort {

    private final HasSessionsInCategoryQuery hasSessionsInCategoryQuery;

    CategoryInUseAdapter(HasSessionsInCategoryQuery hasSessionsInCategoryQuery) {
        this.hasSessionsInCategoryQuery = hasSessionsInCategoryQuery;
    }

    @Override
    public boolean isInUse(UUID categoryId) {
        return hasSessionsInCategoryQuery.hasSessionsInCategory(categoryId);
    }
}
