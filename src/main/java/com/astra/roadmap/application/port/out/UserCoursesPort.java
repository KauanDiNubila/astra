package com.astra.roadmap.application.port.out;

import java.util.List;
import java.util.UUID;

public interface UserCoursesPort {

    boolean isOwner(UUID courseId, UUID userId);

    List<UUID> courseIdsOf(UUID userId);
}
