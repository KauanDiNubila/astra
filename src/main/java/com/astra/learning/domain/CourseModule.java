package com.astra.learning.domain;

import com.astra.shared.exception.NotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public class CourseModule {

    private final UUID id;
    private String title;
    private final int position;
    private boolean markedCompleted;
    private final List<Lesson> lessons;

    private CourseModule(UUID id, String title, int position, boolean markedCompleted, List<Lesson> lessons) {
        this.id = Objects.requireNonNull(id);
        this.title = Objects.requireNonNull(title);
        this.position = position;
        this.markedCompleted = markedCompleted;
        this.lessons = new ArrayList<>(lessons);
        this.lessons.sort(Comparator.comparingInt(Lesson::getPosition));
    }

    static CourseModule create(String title, int position, int lessonCount) {
        List<Lesson> lessons = new ArrayList<>();
        for (int i = 1; i <= lessonCount; i++) {
            lessons.add(Lesson.create("Aula " + i, i));
        }
        return new CourseModule(UUID.randomUUID(), title, position, false, lessons);
    }

    public static CourseModule withId(UUID id, String title, int position, boolean markedCompleted,
                                      List<Lesson> lessons) {
        return new CourseModule(id, title, position, markedCompleted, lessons);
    }

    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }

    public long totalLessons() {
        return lessons.size();
    }

    public long completedLessons() {
        return lessons.stream().filter(Lesson::isCompleted).count();
    }

    public boolean isCompleted() {
        return markedCompleted || (!lessons.isEmpty() && completedLessons() == totalLessons());
    }

    Lesson lesson(UUID lessonId) {
        return lessons.stream()
                .filter(l -> l.getId().equals(lessonId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Lesson not found"));
    }

    Lesson addLesson(String title, int position) {
        markedCompleted = false;
        Lesson lesson = Lesson.create(title, position);
        lessons.add(lesson);
        lessons.sort(Comparator.comparingInt(Lesson::getPosition));
        return lesson;
    }

    void removeLesson(UUID lessonId) {
        lessons.remove(lesson(lessonId));
        for (int i = 0; i < lessons.size(); i++) {
            lessons.get(i).moveTo(i + 1);
        }
    }

    void completeUpTo(int position) {
        for (Lesson lesson : lessons) {
            lesson.markCompleted(lesson.getPosition() <= position);
        }
    }

    void rename(String title) {
        String trimmed = title.trim();
        if (trimmed.isEmpty()) {
            throw new BlankModuleTitleException();
        }
        this.title = trimmed;
    }

    void markCompleted(boolean completed) {
        this.markedCompleted = completed;
    }
}
