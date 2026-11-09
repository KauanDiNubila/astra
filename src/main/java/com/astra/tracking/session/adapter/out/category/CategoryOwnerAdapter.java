package com.astra.tracking.session.adapter.out.category;

import com.astra.tracking.category.application.port.in.CheckCategoryOwnerQuery;
import com.astra.tracking.session.application.port.out.CheckCategoryOwnerPort;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class CategoryOwnerAdapter implements CheckCategoryOwnerPort {

    private final CheckCategoryOwnerQuery checkCategoryOwnerQuery;

    CategoryOwnerAdapter(CheckCategoryOwnerQuery checkCategoryOwnerQuery) {
        this.checkCategoryOwnerQuery = checkCategoryOwnerQuery;
    }

    @Override
    public boolean isOwner(UUID categoryId, UUID userId) {
        return checkCategoryOwnerQuery.isOwner(categoryId, userId);
    }
}
