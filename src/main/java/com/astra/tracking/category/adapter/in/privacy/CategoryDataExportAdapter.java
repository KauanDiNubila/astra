package com.astra.tracking.category.adapter.in.privacy;

import com.astra.tracking.category.application.port.in.ListCategoriesQuery;
import com.astra.privacy.application.port.out.ExportUserDataPort;
import com.astra.shared.ExportRow;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
class CategoryDataExportAdapter implements ExportUserDataPort {

    private final ListCategoriesQuery listCategoriesQuery;

    CategoryDataExportAdapter(ListCategoriesQuery listCategoriesQuery) {
        this.listCategoriesQuery = listCategoriesQuery;
    }

    @Override
    public Map<String, Object> export(UUID userId) {
        return ExportRow.of("categories", listCategoriesQuery.list(userId));
    }
}
