package com.astra.tracking.category.application.port.in;

import java.util.UUID;

public interface CheckCategoryOwnerQuery {

    boolean isOwner(UUID categoryId, UUID userId);
}
