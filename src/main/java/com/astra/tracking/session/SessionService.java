package com.astra.tracking.session;

import com.astra.github.GitHubRepositoryRepository;
import com.astra.learning.CourseService;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import com.astra.tracking.category.Category;
import com.astra.tracking.category.CategoryRepository;
import com.astra.tracking.session.dto.CreateSessionRequest;
import com.astra.tracking.session.dto.SessionResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CategoryRepository categoryRepository;
    private final CourseService courseService;
    private final GitHubRepositoryRepository githubRepositoryRepository;
    private final CurrentUserProvider currentUserProvider;

    public SessionService(SessionRepository sessionRepository, CategoryRepository categoryRepository,
                          CourseService courseService, GitHubRepositoryRepository githubRepositoryRepository,
                          CurrentUserProvider currentUserProvider) {
        this.sessionRepository = sessionRepository;
        this.categoryRepository = categoryRepository;
        this.courseService = courseService;
        this.githubRepositoryRepository = githubRepositoryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public SessionResponse register(CreateSessionRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        Category category = categoryRepository.findById(request.categoryId())
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Category not found"));

        UUID courseId = request.courseId();
        if (courseId != null && !courseService.existsForUser(courseId, userId)) {
            throw new NotFoundException("Course not found");
        }

        Session session = new Session(userId, category, courseId, request.focusedMinutes(),
                request.startedAt(), request.note());
        Session saved = sessionRepository.saveAndFlush(session);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> listForCurrentUser() {
        UUID userId = currentUserProvider.currentUserId();
        return sessionRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public void delete(UUID id) {
        UUID userId = currentUserProvider.currentUserId();
        Session session = sessionRepository.findById(id)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Session not found"));
        sessionRepository.delete(session);
    }

    @Transactional
    public SessionResponse linkGithubRepository(UUID id, UUID repositoryId) {
        Session session = requireOwnedSession(id);
        boolean ownsRepository = githubRepositoryRepository.findById(repositoryId)
                .filter(r -> r.getUserId().equals(session.getUserId()))
                .isPresent();
        if (!ownsRepository) {
            throw new NotFoundException("Repositório não encontrado");
        }
        session.setGithubRepositoryId(repositoryId);
        return toDto(session);
    }

    @Transactional
    public SessionResponse unlinkGithubRepository(UUID id) {
        Session session = requireOwnedSession(id);
        session.setGithubRepositoryId(null);
        return toDto(session);
    }

    @Transactional(readOnly = true)
    public Session requireOwnedSession(UUID id) {
        UUID userId = currentUserProvider.currentUserId();
        return sessionRepository.findById(id)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Session not found"));
    }

    private SessionResponse toDto(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getCategory().getId(),
                session.getCourseId(),
                session.getGithubRepositoryId(),
                session.getFocusedMinutes(),
                session.getStartedAt(),
                session.getNote(),
                session.getCreatedAt());
    }
}
