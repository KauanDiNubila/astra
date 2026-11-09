package com.astra.tracking.session.application.port.out;

import java.util.UUID;

public interface CheckCategoryOwnerPort {

    boolean isOwner(UUID categoryId, UUID userId);
}
