package com.astra.learning.application.port.in;

import java.util.UUID;

public interface CheckCourseOwnerQuery {

    boolean isOwner(UUID courseId, UUID userId);
}
