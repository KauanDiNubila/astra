package com.astra.learning.application.port.in;

import java.util.UUID;

public interface RemoveLessonUseCase {

    void remove(UUID userId, UUID courseId, UUID moduleId, UUID lessonId);
}
