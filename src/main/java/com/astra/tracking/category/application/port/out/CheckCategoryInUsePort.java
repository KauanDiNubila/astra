package com.astra.tracking.category.application.port.out;

import java.util.UUID;

public interface CheckCategoryInUsePort {

    boolean isInUse(UUID categoryId);
}
