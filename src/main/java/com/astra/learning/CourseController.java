package com.astra.learning;

import com.astra.learning.dto.CourseDetailResponse;
import com.astra.learning.dto.CourseResponse;
import com.astra.learning.dto.CreateCourseRequest;
import com.astra.learning.dto.CreateLessonRequest;
import com.astra.learning.dto.CreateModuleRequest;
import com.astra.learning.dto.LessonResponse;
import com.astra.learning.dto.ModuleProgressRequest;
import com.astra.learning.dto.ModuleResponse;
import com.astra.learning.dto.UpdateLessonRequest;
import com.astra.learning.dto.UpdateModuleRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CreateCourseRequest request) {
        return courseService.create(request);
    }

    @GetMapping
    public List<CourseResponse> list() {
        return courseService.list();
    }

    @GetMapping("/{id}")
    public CourseDetailResponse get(@PathVariable UUID id) {
        return courseService.get(id);
    }

    @PostMapping("/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleResponse addModule(@PathVariable UUID id, @Valid @RequestBody CreateModuleRequest request) {
        return courseService.addModule(id, request);
    }

    @PostMapping("/{courseId}/modules/{moduleId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonResponse addLesson(@PathVariable UUID courseId, @PathVariable UUID moduleId,
                                    @Valid @RequestBody CreateLessonRequest request) {
        return courseService.addLesson(courseId, moduleId, request);
    }

    @PatchMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}")
    public LessonResponse updateLesson(@PathVariable UUID courseId, @PathVariable UUID moduleId,
                                       @PathVariable UUID lessonId, @Valid @RequestBody UpdateLessonRequest request) {
        return courseService.setLessonCompleted(courseId, moduleId, lessonId, request.completed());
    }

    @DeleteMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable UUID courseId, @PathVariable UUID moduleId, @PathVariable UUID lessonId) {
        courseService.deleteLesson(courseId, moduleId, lessonId);
    }

    @PatchMapping("/{courseId}/modules/{moduleId}")
    public ModuleResponse updateModule(@PathVariable UUID courseId, @PathVariable UUID moduleId,
                                       @Valid @RequestBody UpdateModuleRequest request) {
        return courseService.updateModule(courseId, moduleId, request);
    }

    @PatchMapping("/{courseId}/modules/{moduleId}/progress")
    public ModuleResponse updateModuleProgress(@PathVariable UUID courseId, @PathVariable UUID moduleId,
                                               @Valid @RequestBody ModuleProgressRequest request) {
        return courseService.setModuleProgress(courseId, moduleId, request.uptoPosition());
    }
}
