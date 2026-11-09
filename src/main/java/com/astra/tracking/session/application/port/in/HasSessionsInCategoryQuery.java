package com.astra.tracking.session.application.port.in;

import java.util.UUID;

public interface HasSessionsInCategoryQuery {

    boolean hasSessionsInCategory(UUID categoryId);
}
