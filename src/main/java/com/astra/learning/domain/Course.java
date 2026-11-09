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
public class Course {

    private final UUID id;
    private final UUID userId;
    private final String title;
    private final String platform;
    private final CourseStatus status;
    private final List<CourseModule> modules;

    private Course(UUID id, UUID userId, String title, String platform, CourseStatus status,
                   List<CourseModule> modules) {
        this.id = Objects.requireNonNull(id);
        this.userId = Objects.requireNonNull(userId);
        this.title = Objects.requireNonNull(title);
        this.platform = platform;
        this.status = status;
        this.modules = new ArrayList<>(modules);
        this.modules.sort(Comparator.comparingInt(CourseModule::getPosition));
    }

    public static Course plan(UUID userId, String title, String platform, int moduleCount) {
        List<CourseModule> modules = new ArrayList<>();
        for (int i = 1; i <= moduleCount; i++) {
            modules.add(CourseModule.create("Módulo " + i, i, 0));
        }
        return new Course(UUID.randomUUID(), userId, title, platform, CourseStatus.PLANNED, modules);
    }

    public static Course withId(UUID id, UUID userId, String title, String platform, CourseStatus status,
                                List<CourseModule> modules) {
        return new Course(id, userId, title, platform, status, modules);
    }

    public List<CourseModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public boolean belongsTo(UUID userId) {
        return this.userId.equals(userId);
    }

    public long totalLessons() {
        return modules.stream().mapToLong(CourseModule::totalLessons).sum();
    }

    public long completedLessons() {
        return modules.stream().mapToLong(CourseModule::completedLessons).sum();
    }

    public double progress() {
        long total = totalLessons();
        return total == 0 ? 0.0 : (double) completedLessons() / total;
    }

    public CourseModule module(UUID moduleId) {
        return modules.stream()
                .filter(m -> m.getId().equals(moduleId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Module not found"));
    }

    public CourseModule addModule(String title, int position, int lessonCount) {
        CourseModule module = CourseModule.create(title, position, lessonCount);
        modules.add(module);
        modules.sort(Comparator.comparingInt(CourseModule::getPosition));
        return module;
    }

    public Lesson addLesson(UUID moduleId, String title, int position) {
        return module(moduleId).addLesson(title, position);
    }

    public Lesson markLessonCompleted(UUID moduleId, UUID lessonId, boolean completed) {
        Lesson lesson = module(moduleId).lesson(lessonId);
        lesson.markCompleted(completed);
        return lesson;
    }

    public void removeLesson(UUID moduleId, UUID lessonId) {
        module(moduleId).removeLesson(lessonId);
    }

    public CourseModule completeLessonsUpTo(UUID moduleId, int position) {
        CourseModule module = module(moduleId);
        module.completeUpTo(position);
        return module;
    }

    public CourseModule renameModule(UUID moduleId, String title) {
        CourseModule module = module(moduleId);
        module.rename(title);
        return module;
    }

    public CourseModule markModuleCompleted(UUID moduleId, boolean completed) {
        CourseModule module = module(moduleId);
        module.markCompleted(completed);
        return module;
    }
}
