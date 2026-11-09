package com.astra.tracking.category.application.port.out;

import java.util.UUID;

public interface CheckCategoryNameTakenPort {

    boolean isNameTaken(UUID userId, String name);
}
