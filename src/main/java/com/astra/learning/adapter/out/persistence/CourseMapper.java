package com.astra.learning.adapter.out.persistence;

import com.astra.learning.domain.Course;
import com.astra.learning.domain.CourseModule;
import com.astra.learning.domain.Lesson;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class CourseMapper {

    Course toDomain(CourseJpaEntity entity) {
        List<CourseModule> modules = entity.getModules().stream()
                .map(m -> CourseModule.withId(m.getId(), m.getTitle(), m.getPosition(), m.isCompleted(),
                        m.getLessons().stream()
                                .map(l -> Lesson.withId(l.getId(), l.getTitle(), l.getPosition(), l.isCompleted()))
                                .toList()))
                .toList();
        return Course.withId(entity.getId(), entity.getUserId(), entity.getTitle(), entity.getPlatform(),
                entity.getStatus(), modules);
    }

    CourseJpaEntity toNewJpaEntity(Course course) {
        CourseJpaEntity entity = new CourseJpaEntity(course.getId(), course.getUserId(), course.getTitle(),
                course.getPlatform(), course.getStatus(), new ArrayList<>());
        copyModules(course, entity);
        return entity;
    }

    void copyModules(Course course, CourseJpaEntity entity) {
        Map<UUID, ModuleJpaEntity> existing = byId(entity.getModules(), ModuleJpaEntity::getId);
        Set<UUID> kept = course.getModules().stream().map(CourseModule::getId).collect(Collectors.toSet());
        entity.getModules().removeIf(m -> !kept.contains(m.getId()));
        for (CourseModule module : course.getModules()) {
            ModuleJpaEntity target = existing.get(module.getId());
            if (target == null) {
                target = new ModuleJpaEntity(module.getId(), entity);
                entity.getModules().add(target);
            }
            target.setTitle(module.getTitle());
            target.setPosition(module.getPosition());
            target.setCompleted(module.isMarkedCompleted());
            copyLessons(module, target);
        }
    }

    private void copyLessons(CourseModule module, ModuleJpaEntity target) {
        Map<UUID, LessonJpaEntity> existing = byId(target.getLessons(), LessonJpaEntity::getId);
        Set<UUID> kept = module.getLessons().stream().map(Lesson::getId).collect(Collectors.toSet());
        target.getLessons().removeIf(l -> !kept.contains(l.getId()));
        for (Lesson lesson : module.getLessons()) {
            LessonJpaEntity row = existing.get(lesson.getId());
            if (row == null) {
                row = new LessonJpaEntity(lesson.getId(), target);
                target.getLessons().add(row);
            }
            row.setTitle(lesson.getTitle());
            row.setPosition(lesson.getPosition());
            row.setCompleted(lesson.isCompleted());
        }
    }

    private static <T> Map<UUID, T> byId(List<T> items, Function<T, UUID> id) {
        return items.stream().collect(Collectors.toMap(id, Function.identity()));
    }
}
