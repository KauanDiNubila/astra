package com.astra.learning;

import com.astra.learning.dto.CourseDetailResponse;
import com.astra.learning.dto.CourseResponse;
import com.astra.learning.dto.CreateCourseRequest;
import com.astra.learning.dto.CreateLessonRequest;
import com.astra.learning.dto.CreateModuleRequest;
import com.astra.learning.dto.LessonResponse;
import com.astra.learning.dto.ModuleResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CurrentUserProvider currentUserProvider;

    public CourseService(CourseRepository courseRepository, CourseModuleRepository moduleRepository,
                         LessonRepository lessonRepository, CurrentUserProvider currentUserProvider) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public CourseResponse create(CreateCourseRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        Course course = new Course(userId, request.title(), request.platform(), CourseStatus.PLANNED);
        Course saved = courseRepository.save(course);
        return toSummary(saved, 0, 0);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> list() {
        UUID userId = currentUserProvider.currentUserId();
        return courseRepository.findByUserId(userId).stream()
                .map(c -> {
                    List<UUID> moduleIds = moduleRepository.findByCourseIdOrderByPosition(c.getId()).stream()
                            .map(CourseModule::getId)
                            .toList();
                    long total = lessonRepository.countByModuleIdIn(moduleIds);
                    long completed = lessonRepository.countByModuleIdInAndCompletedTrue(moduleIds);
                    return toSummary(c, total, completed);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse get(UUID courseId) {
        Course course = ownedCourse(courseId);
        List<CourseModule> modules = moduleRepository.findByCourseIdOrderByPosition(courseId);
        List<UUID> moduleIds = modules.stream().map(CourseModule::getId).toList();
        List<Lesson> allLessons = lessonRepository.findByModuleIdInOrderByPosition(moduleIds);
        Map<UUID, List<Lesson>> lessonsByModule = allLessons.stream()
                .collect(Collectors.groupingBy(l -> l.getModule().getId()));

        List<ModuleResponse> moduleResponses = modules.stream()
                .map(m -> toModuleResponse(m, lessonsByModule.getOrDefault(m.getId(), List.of())))
                .toList();

        long total = allLessons.size();
        long completed = allLessons.stream().filter(Lesson::isCompleted).count();
        return new CourseDetailResponse(course.getId(), course.getTitle(), course.getPlatform(),
                course.getStatus(), progress(total, completed), total, completed, moduleResponses);
    }

    @Transactional
    public ModuleResponse addModule(UUID courseId, CreateModuleRequest request) {
        Course course = ownedCourse(courseId);
        CourseModule module = new CourseModule(course, request.title(), request.position());
        CourseModule saved = moduleRepository.save(module);
        return toModuleResponse(saved, List.of());
    }

    @Transactional
    public LessonResponse addLesson(UUID courseId, UUID moduleId, CreateLessonRequest request) {
        ownedCourse(courseId);
        CourseModule module = moduleRepository.findByIdAndCourseId(moduleId, courseId)
                .orElseThrow(() -> new NotFoundException("Module not found"));
        if (module.isCompleted()) {
            module.setCompleted(false);
            moduleRepository.save(module);
        }
        Lesson lesson = new Lesson(module, request.title(), request.position());
        Lesson saved = lessonRepository.save(lesson);
        return toLessonResponse(saved);
    }

    @Transactional
    public LessonResponse setLessonCompleted(UUID courseId, UUID moduleId, UUID lessonId, boolean completed) {
        ownedCourse(courseId);
        moduleRepository.findByIdAndCourseId(moduleId, courseId)
                .orElseThrow(() -> new NotFoundException("Module not found"));
        Lesson lesson = lessonRepository.findByIdAndModuleId(lessonId, moduleId)
                .orElseThrow(() -> new NotFoundException("Lesson not found"));
        lesson.setCompleted(completed);
        Lesson saved = lessonRepository.save(lesson);
        return toLessonResponse(saved);
    }

    @Transactional
    public ModuleResponse setModuleCompleted(UUID courseId, UUID moduleId, boolean completed) {
        ownedCourse(courseId);
        CourseModule module = moduleRepository.findByIdAndCourseId(moduleId, courseId)
                .orElseThrow(() -> new NotFoundException("Module not found"));
        module.setCompleted(completed);
        CourseModule saved = moduleRepository.save(module);
        List<Lesson> lessons = lessonRepository.findByModuleIdInOrderByPosition(List.of(moduleId));
        return toModuleResponse(saved, lessons);
    }

    @Transactional(readOnly = true)
    public boolean existsForUser(UUID courseId, UUID userId) {
        return courseRepository.existsByIdAndUserId(courseId, userId);
    }

    @Transactional(readOnly = true)
    public List<UUID> courseIdsForUser(UUID userId) {
        return courseRepository.findIdsByUserId(userId);
    }

    private Course ownedCourse(UUID courseId) {
        UUID userId = currentUserProvider.currentUserId();
        return courseRepository.findByIdAndUserId(courseId, userId)
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    private CourseResponse toSummary(Course course, long total, long completed) {
        return new CourseResponse(course.getId(), course.getTitle(), course.getPlatform(),
                course.getStatus(), progress(total, completed), total, completed);
    }

    private ModuleResponse toModuleResponse(CourseModule module, List<Lesson> lessons) {
        long total = lessons.size();
        long completed = lessons.stream().filter(Lesson::isCompleted).count();
        boolean allLessonsDone = total > 0 && completed == total;
        List<LessonResponse> lessonResponses = lessons.stream().map(this::toLessonResponse).toList();
        return new ModuleResponse(module.getId(), module.getTitle(), module.getPosition(),
                total, completed, module.isCompleted() || allLessonsDone, lessonResponses);
    }

    private LessonResponse toLessonResponse(Lesson lesson) {
        return new LessonResponse(lesson.getId(), lesson.getTitle(), lesson.getPosition(), lesson.isCompleted());
    }

    private double progress(long total, long completed) {
        return total == 0 ? 0.0 : (double) completed / total;
    }
}
