package com.astra.roadmap.application.port.in;

import java.util.UUID;

public interface UnpinCourseUseCase {

    void unpin(UUID userId, UUID stepId, UUID pinId);
}
