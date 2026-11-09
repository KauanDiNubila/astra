package com.astra.learning.application.port.in;

import java.util.UUID;

public interface CompleteLessonsUpToUseCase {

    ModuleResponse completeUpTo(UUID userId, UUID courseId, UUID moduleId, int position);
}
