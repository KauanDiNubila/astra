package com.astra.learning.adapter.in.web;

import com.astra.learning.application.port.in.AddLessonUseCase;
import com.astra.learning.application.port.in.AddModuleUseCase;
import com.astra.learning.application.port.in.CompleteLessonUseCase;
import com.astra.learning.application.port.in.CompleteLessonsUpToUseCase;
import com.astra.learning.application.port.in.CourseDetailResponse;
import com.astra.learning.application.port.in.CourseRepositoriesResponse;
import com.astra.learning.application.port.in.CourseResponse;
import com.astra.learning.application.port.in.CreateCourseUseCase;
import com.astra.learning.application.port.in.GetCourseQuery;
import com.astra.learning.application.port.in.GetCourseRepositoriesQuery;
import com.astra.learning.application.port.in.LessonResponse;
import com.astra.learning.application.port.in.LinkGithubRepositoryToCourseUseCase;
import com.astra.learning.application.port.in.ListCoursesQuery;
import com.astra.learning.application.port.in.ModuleResponse;
import com.astra.learning.application.port.in.RemoveLessonUseCase;
import com.astra.learning.application.port.in.UnlinkGithubRepositoryFromCourseUseCase;
import com.astra.learning.application.port.in.UpdateModuleUseCase;
import com.astra.shared.CurrentUserProvider;
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
class CourseController {

    private final CreateCourseUseCase createCourseUseCase;
    private final AddModuleUseCase addModuleUseCase;
    private final AddLessonUseCase addLessonUseCase;
    private final CompleteLessonUseCase completeLessonUseCase;
    private final RemoveLessonUseCase removeLessonUseCase;
    private final UpdateModuleUseCase updateModuleUseCase;
    private final CompleteLessonsUpToUseCase completeLessonsUpToUseCase;
    private final LinkGithubRepositoryToCourseUseCase linkGithubRepositoryUseCase;
    private final UnlinkGithubRepositoryFromCourseUseCase unlinkGithubRepositoryUseCase;
    private final ListCoursesQuery listCoursesQuery;
    private final GetCourseQuery getCourseQuery;
    private final GetCourseRepositoriesQuery getCourseRepositoriesQuery;
    private final CurrentUserProvider currentUserProvider;

    CourseController(CreateCourseUseCase createCourseUseCase, AddModuleUseCase addModuleUseCase,
                     AddLessonUseCase addLessonUseCase, CompleteLessonUseCase completeLessonUseCase,
                     RemoveLessonUseCase removeLessonUseCase, UpdateModuleUseCase updateModuleUseCase,
                     CompleteLessonsUpToUseCase completeLessonsUpToUseCase,
                     LinkGithubRepositoryToCourseUseCase linkGithubRepositoryUseCase,
                     UnlinkGithubRepositoryFromCourseUseCase unlinkGithubRepositoryUseCase,
                     ListCoursesQuery listCoursesQuery, GetCourseQuery getCourseQuery,
                     GetCourseRepositoriesQuery getCourseRepositoriesQuery,
                     CurrentUserProvider currentUserProvider) {
        this.createCourseUseCase = createCourseUseCase;
        this.addModuleUseCase = addModuleUseCase;
        this.addLessonUseCase = addLessonUseCase;
        this.completeLessonUseCase = completeLessonUseCase;
        this.removeLessonUseCase = removeLessonUseCase;
        this.updateModuleUseCase = updateModuleUseCase;
        this.completeLessonsUpToUseCase = completeLessonsUpToUseCase;
        this.linkGithubRepositoryUseCase = linkGithubRepositoryUseCase;
        this.unlinkGithubRepositoryUseCase = unlinkGithubRepositoryUseCase;
        this.listCoursesQuery = listCoursesQuery;
        this.getCourseQuery = getCourseQuery;
        this.getCourseRepositoriesQuery = getCourseRepositoriesQuery;
        this.currentUserProvider = currentUserProvider;
    }

    private UUID me() {
        return currentUserProvider.currentUserId();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CourseResponse create(@Valid @RequestBody CreateCourseRequest request) {
        return createCourseUseCase.create(request.toCommand(me()));
    }

    @GetMapping
    List<CourseResponse> list() {
        return listCoursesQuery.list(me());
    }

    @GetMapping("/{id}")
    CourseDetailResponse get(@PathVariable UUID id) {
        return getCourseQuery.get(me(), id);
    }

    @PostMapping("/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    ModuleResponse addModule(@PathVariable UUID id, @Valid @RequestBody CreateModuleRequest request) {
        return addModuleUseCase.addModule(request.toCommand(me(), id));
    }

    @PostMapping("/{courseId}/modules/{moduleId}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    LessonResponse addLesson(@PathVariable UUID courseId, @PathVariable UUID moduleId,
                             @Valid @RequestBody CreateLessonRequest request) {
        return addLessonUseCase.addLesson(request.toCommand(me(), courseId, moduleId));
    }

    @PatchMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}")
    LessonResponse updateLesson(@PathVariable UUID courseId, @PathVariable UUID moduleId,
                                @PathVariable UUID lessonId, @Valid @RequestBody UpdateLessonRequest request) {
        return completeLessonUseCase.markCompleted(me(), courseId, moduleId, lessonId, request.completed());
    }

    @DeleteMapping("/{courseId}/modules/{moduleId}/lessons/{lessonId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteLesson(@PathVariable UUID courseId, @PathVariable UUID moduleId, @PathVariable UUID lessonId) {
        removeLessonUseCase.remove(me(), courseId, moduleId, lessonId);
    }

    @PatchMapping("/{courseId}/modules/{moduleId}")
    ModuleResponse updateModule(@PathVariable UUID courseId, @PathVariable UUID moduleId,
                                @Valid @RequestBody UpdateModuleRequest request) {
        return updateModuleUseCase.update(request.toCommand(me(), courseId, moduleId));
    }

    @PatchMapping("/{courseId}/modules/{moduleId}/progress")
    ModuleResponse updateModuleProgress(@PathVariable UUID courseId, @PathVariable UUID moduleId,
                                        @Valid @RequestBody ModuleProgressRequest request) {
        return completeLessonsUpToUseCase.completeUpTo(me(), courseId, moduleId, request.uptoPosition());
    }

    @GetMapping("/{id}/github-repositories")
    CourseRepositoriesResponse githubRepositories(@PathVariable UUID id) {
        return getCourseRepositoriesQuery.repositories(me(), id);
    }

    @PostMapping("/{id}/github-repositories")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void linkGithubRepository(@PathVariable UUID id, @Valid @RequestBody LinkGithubRepositoryRequest request) {
        linkGithubRepositoryUseCase.link(me(), id, request.repositoryId());
    }

    @DeleteMapping("/{id}/github-repositories/{repositoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void unlinkGithubRepository(@PathVariable UUID id, @PathVariable UUID repositoryId) {
        unlinkGithubRepositoryUseCase.unlink(me(), id, repositoryId);
    }
}
