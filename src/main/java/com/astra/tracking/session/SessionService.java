package com.astra.tracking.session;

import com.astra.shared.CurrentUserProvider;
import com.astra.tracking.category.Category;
import com.astra.tracking.category.CategoryRepository;
import com.astra.tracking.session.dto.CreateSessionRequest;
import com.astra.tracking.session.dto.SessionResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final CategoryRepository categoryRepository;
    private final CurrentUserProvider currentUserProvider;

    public SessionService(SessionRepository sessionRepository,
                          CategoryRepository categoryRepository,
                          CurrentUserProvider currentUserProvider) {
        this.sessionRepository = sessionRepository;
        this.categoryRepository = categoryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional
    public SessionResponse register(CreateSessionRequest request) {
        UUID userId = currentUserProvider.currentUserId();
        Category category = categoryRepository.findById(request.categoryId())
                .filter(c -> c.getUserId().equals(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        Session session = new Session(userId, category, request.focusedMinutes(),
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

    private SessionResponse toDto(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getCategory().getId(),
                session.getFocusedMinutes(),
                session.getStartedAt(),
                session.getNote(),
                session.getCreatedAt());
    }
}
