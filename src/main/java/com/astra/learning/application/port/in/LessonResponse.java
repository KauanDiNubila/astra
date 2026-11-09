package com.astra.learning.application.port.in;

import com.astra.learning.domain.Lesson;
import java.util.UUID;

public record LessonResponse(UUID id, String title, int position, boolean completed) {

    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(lesson.getId(), lesson.getTitle(), lesson.getPosition(), lesson.isCompleted());
    }
}
