package com.astra.github.adapter.in.web;

import com.astra.github.application.port.in.ChangeGitHubVisibilityUseCase;
import com.astra.github.application.port.in.ConnectGitHubUseCase;
import com.astra.github.application.port.in.DisconnectGitHubUseCase;
import com.astra.github.application.port.in.GetConnectUrlQuery;
import com.astra.github.application.port.in.GetGitHubStatusQuery;
import com.astra.github.application.port.in.GitHubConnectionStatus;
import com.astra.github.application.port.in.SyncGitHubUseCase;
import com.astra.shared.CurrentUserProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/github")
class GitHubConnectionController {

    record AuthorizeUrl(String url) {
    }

    record VisibilityRequest(boolean visibleToFriends) {
    }

    private final GetConnectUrlQuery getConnectUrlQuery;
    private final ConnectGitHubUseCase connectGitHubUseCase;
    private final SyncGitHubUseCase syncGitHubUseCase;
    private final GetGitHubStatusQuery getGitHubStatusQuery;
    private final ChangeGitHubVisibilityUseCase changeGitHubVisibilityUseCase;
    private final DisconnectGitHubUseCase disconnectGitHubUseCase;
    private final CurrentUserProvider currentUserProvider;
    private final String frontendUrl;

    GitHubConnectionController(GetConnectUrlQuery getConnectUrlQuery, ConnectGitHubUseCase connectGitHubUseCase,
                               SyncGitHubUseCase syncGitHubUseCase, GetGitHubStatusQuery getGitHubStatusQuery,
                               ChangeGitHubVisibilityUseCase changeGitHubVisibilityUseCase,
                               DisconnectGitHubUseCase disconnectGitHubUseCase, CurrentUserProvider currentUserProvider,
                               @Value("${astra.frontend-url}") String frontendUrl) {
        this.getConnectUrlQuery = getConnectUrlQuery;
        this.connectGitHubUseCase = connectGitHubUseCase;
        this.syncGitHubUseCase = syncGitHubUseCase;
        this.getGitHubStatusQuery = getGitHubStatusQuery;
        this.changeGitHubVisibilityUseCase = changeGitHubVisibilityUseCase;
        this.disconnectGitHubUseCase = disconnectGitHubUseCase;
        this.currentUserProvider = currentUserProvider;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/connect/authorize-url")
    AuthorizeUrl authorizeUrl() {
        return new AuthorizeUrl(getConnectUrlQuery.connectUrl(currentUserProvider.currentUserId()));
    }

    @GetMapping("/connect/callback")
    void callback(@RequestParam String code, @RequestParam String state, HttpServletResponse response)
            throws IOException {
        UUID userId;
        try {
            userId = connectGitHubUseCase.connect(code, state);
        } catch (RuntimeException e) {
            response.sendRedirect(frontendUrl + "/dashboard?githubError=1");
            return;
        }
        try {
            syncGitHubUseCase.sync(userId);
        } catch (RuntimeException ignoredSyncFailure) {
        }
        response.sendRedirect(frontendUrl + "/dashboard");
    }

    @GetMapping("/status")
    GitHubConnectionStatus status() {
        return getGitHubStatusQuery.status(currentUserProvider.currentUserId());
    }

    @DeleteMapping("/connection")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void disconnect() {
        disconnectGitHubUseCase.disconnect(currentUserProvider.currentUserId());
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void sync() {
        syncGitHubUseCase.sync(currentUserProvider.currentUserId());
    }

    @PatchMapping("/visibility")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void changeVisibility(@RequestBody VisibilityRequest request) {
        changeGitHubVisibilityUseCase.change(currentUserProvider.currentUserId(), request.visibleToFriends());
    }
}
