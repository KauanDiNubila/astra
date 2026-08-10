package com.astra.learning;

import com.astra.learning.dto.CourseDetailResponse;
import com.astra.learning.dto.CourseResponse;
import com.astra.learning.dto.CreateCourseRequest;
import com.astra.learning.dto.CreateModuleRequest;
import com.astra.learning.dto.ModuleResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final CurrentUserProvider currentUserProvider;

    public CourseService(CourseRepository courseRepository, CourseModuleRepository moduleRepository,
                         CurrentUserProvider currentUserProvider) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
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
                .map(c -> toSummary(c,
                        moduleRepository.countByCourseId(c.getId()),
                        moduleRepository.countByCourseIdAndCompletedTrue(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public CourseDetailResponse get(UUID courseId) {
        Course course = ownedCourse(courseId);
        List<ModuleResponse> modules = moduleRepository.findByCourseIdOrderByPosition(courseId).stream()
                .map(m -> new ModuleResponse(m.getId(), m.getTitle(), m.getPosition(), m.isCompleted()))
                .toList();
        long total = modules.size();
        long completed = modules.stream().filter(ModuleResponse::completed).count();
        return new CourseDetailResponse(course.getId(), course.getTitle(), course.getPlatform(),
                course.getStatus(), progress(total, completed), total, completed, modules);
    }

    @Transactional
    public ModuleResponse addModule(UUID courseId, CreateModuleRequest request) {
        Course course = ownedCourse(courseId);
        CourseModule module = new CourseModule(course, request.title(), request.position());
        CourseModule saved = moduleRepository.save(module);
        return new ModuleResponse(saved.getId(), saved.getTitle(), saved.getPosition(), saved.isCompleted());
    }

    @Transactional
    public ModuleResponse setModuleCompleted(UUID courseId, UUID moduleId, boolean completed) {
        ownedCourse(courseId);
        CourseModule module = moduleRepository.findByIdAndCourseId(moduleId, courseId)
                .orElseThrow(() -> new NotFoundException("Module not found"));
        module.setCompleted(completed);
        CourseModule saved = moduleRepository.save(module);
        return new ModuleResponse(saved.getId(), saved.getTitle(), saved.getPosition(), saved.isCompleted());
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

    private double progress(long total, long completed) {
        return total == 0 ? 0.0 : (double) completed / total;
    }
}
