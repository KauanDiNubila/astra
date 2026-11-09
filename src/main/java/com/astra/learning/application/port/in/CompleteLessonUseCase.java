package com.astra.learning.application.port.in;

import java.util.UUID;

public interface CompleteLessonUseCase {

    LessonResponse markCompleted(UUID userId, UUID courseId, UUID moduleId, UUID lessonId, boolean completed);
}
