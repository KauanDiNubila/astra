package com.astra.learning.application.port.out;

import java.util.List;
import java.util.UUID;

public interface CheckCourseOwnershipPort {

    boolean isOwner(UUID courseId, UUID userId);

    List<UUID> courseIdsOf(UUID userId);
}
