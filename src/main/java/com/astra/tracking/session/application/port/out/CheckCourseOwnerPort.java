package com.astra.tracking.session.application.port.out;

import java.util.UUID;

public interface CheckCourseOwnerPort {

    boolean isOwner(UUID courseId, UUID userId);
}
