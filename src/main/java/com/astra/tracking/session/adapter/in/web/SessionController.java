package com.astra.tracking.session.adapter.in.web;

import com.astra.github.application.port.in.GetSessionActivityQuery;
import com.astra.github.application.port.in.SessionActivityResponse;
import com.astra.shared.CurrentUserProvider;
import com.astra.tracking.session.application.port.in.DeleteSessionUseCase;
import com.astra.tracking.session.application.port.in.GetSessionWindowQuery;
import com.astra.tracking.session.application.port.in.GetSessionWindowQuery.SessionWindow;
import com.astra.tracking.session.application.port.in.LinkRepositoryToSessionUseCase;
import com.astra.tracking.session.application.port.in.ListSessionsQuery;
import com.astra.tracking.session.application.port.in.RegisterSessionUseCase;
import com.astra.tracking.session.application.port.in.SessionResponse;
import com.astra.tracking.session.application.port.in.UnlinkRepositoryFromSessionUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions")
class SessionController {

    private final RegisterSessionUseCase registerSessionUseCase;
    private final DeleteSessionUseCase deleteSessionUseCase;
    private final LinkRepositoryToSessionUseCase linkRepositoryToSessionUseCase;
    private final UnlinkRepositoryFromSessionUseCase unlinkRepositoryFromSessionUseCase;
    private final ListSessionsQuery listSessionsQuery;
    private final GetSessionWindowQuery getSessionWindowQuery;
    private final GetSessionActivityQuery getSessionActivityQuery;
    private final CurrentUserProvider currentUserProvider;

    SessionController(RegisterSessionUseCase registerSessionUseCase, DeleteSessionUseCase deleteSessionUseCase,
                      LinkRepositoryToSessionUseCase linkRepositoryToSessionUseCase,
                      UnlinkRepositoryFromSessionUseCase unlinkRepositoryFromSessionUseCase,
                      ListSessionsQuery listSessionsQuery, GetSessionWindowQuery getSessionWindowQuery,
                      GetSessionActivityQuery getSessionActivityQuery, CurrentUserProvider currentUserProvider) {
        this.registerSessionUseCase = registerSessionUseCase;
        this.deleteSessionUseCase = deleteSessionUseCase;
        this.linkRepositoryToSessionUseCase = linkRepositoryToSessionUseCase;
        this.unlinkRepositoryFromSessionUseCase = unlinkRepositoryFromSessionUseCase;
        this.listSessionsQuery = listSessionsQuery;
        this.getSessionWindowQuery = getSessionWindowQuery;
        this.getSessionActivityQuery = getSessionActivityQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SessionResponse register(@Valid @RequestBody RegisterSessionRequest request) {
        return registerSessionUseCase.register(request.toCommand(currentUserProvider.currentUserId()));
    }

    @GetMapping
    List<SessionResponse> list(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "30") int size) {
        return listSessionsQuery.list(currentUserProvider.currentUserId(), page, size);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@PathVariable UUID id) {
        deleteSessionUseCase.delete(currentUserProvider.currentUserId(), id);
    }

    @GetMapping("/{id}/github-activity")
    SessionActivityResponse githubActivity(@PathVariable UUID id) {
        UUID userId = currentUserProvider.currentUserId();
        SessionWindow window = getSessionWindowQuery.get(userId, id);
        return getSessionActivityQuery.activityFor(userId, window.startedAt(), window.endedAt(),
                window.githubRepositoryId());
    }

    @PutMapping("/{id}/github-repository")
    SessionResponse linkRepository(@PathVariable UUID id, @Valid @RequestBody LinkRepositoryRequest request) {
        return linkRepositoryToSessionUseCase.link(currentUserProvider.currentUserId(), id, request.repositoryId());
    }

    @DeleteMapping("/{id}/github-repository")
    SessionResponse unlinkRepository(@PathVariable UUID id) {
        return unlinkRepositoryFromSessionUseCase.unlink(currentUserProvider.currentUserId(), id);
    }
}
