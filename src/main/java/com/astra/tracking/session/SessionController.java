package com.astra.tracking.session;

import com.astra.github.GitHubLiveActivityService;
import com.astra.github.dto.LinkGithubRepositoryRequest;
import com.astra.github.dto.SessionGitHubActivity;
import com.astra.tracking.session.dto.CreateSessionRequest;
import com.astra.tracking.session.dto.SessionResponse;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final GitHubLiveActivityService gitHubLiveActivityService;

    public SessionController(SessionService sessionService, GitHubLiveActivityService gitHubLiveActivityService) {
        this.sessionService = sessionService;
        this.gitHubLiveActivityService = gitHubLiveActivityService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse register(@Valid @RequestBody CreateSessionRequest request) {
        return sessionService.register(request);
    }

    @GetMapping
    public List<SessionResponse> list() {
        return sessionService.listForCurrentUser();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        sessionService.delete(id);
    }

    @GetMapping("/{id}/github-activity")
    public SessionGitHubActivity githubActivity(@PathVariable UUID id) {
        Session session = sessionService.requireOwnedSession(id);
        return gitHubLiveActivityService.activityForWindow(session.getUserId(), session.getStartedAt(),
                session.getStartedAt().plusMinutes(session.getFocusedMinutes()), session.getGithubRepositoryId());
    }

    @PutMapping("/{id}/github-repository")
    public SessionResponse linkGithubRepository(@PathVariable UUID id,
            @Valid @RequestBody LinkGithubRepositoryRequest request) {
        return sessionService.linkGithubRepository(id, request.repositoryId());
    }

    @DeleteMapping("/{id}/github-repository")
    public SessionResponse unlinkGithubRepository(@PathVariable UUID id) {
        return sessionService.unlinkGithubRepository(id);
    }
}
