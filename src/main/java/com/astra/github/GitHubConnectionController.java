package com.astra.github;

import com.astra.github.dto.GitHubAuthorizeUrl;
import com.astra.github.dto.GitHubConnectionStatus;
import com.astra.github.dto.UpdateGithubVisibilityRequest;
import com.astra.shared.CurrentUserProvider;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
public class GitHubConnectionController {

    private final GitHubConnectionService connectionService;
    private final GitHubSyncService syncService;
    private final CurrentUserProvider currentUserProvider;
    private final String frontendUrl;

    public GitHubConnectionController(GitHubConnectionService connectionService, GitHubSyncService syncService,
            CurrentUserProvider currentUserProvider, @Value("${astra.frontend-url}") String frontendUrl) {
        this.connectionService = connectionService;
        this.syncService = syncService;
        this.currentUserProvider = currentUserProvider;
        this.frontendUrl = frontendUrl;
    }

    @GetMapping("/connect/authorize-url")
    public GitHubAuthorizeUrl authorizeUrl() {
        return new GitHubAuthorizeUrl(connectionService.buildAuthorizeUrl());
    }

    @GetMapping("/connect/callback")
    public void callback(@RequestParam String code, @RequestParam String state, HttpServletResponse response)
            throws IOException {
        try {
            UUID userId = connectionService.handleCallback(code, state);
            try {
                syncService.sync(userId);
            } catch (RuntimeException syncFailure) {
                // A conexao ja foi salva com sucesso - uma falha no sync
                // inicial nao deve impedir o usuario de ver "conectado" e
                // tentar sincronizar de novo depois.
            }
            response.sendRedirect(frontendUrl + "/dashboard");
        } catch (RuntimeException e) {
            response.sendRedirect(frontendUrl + "/dashboard?githubError=1");
        }
    }

    @GetMapping("/status")
    public GitHubConnectionStatus status() {
        return connectionService.status();
    }

    @DeleteMapping("/connection")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disconnect() {
        connectionService.disconnect();
    }

    @PostMapping("/sync")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sync() {
        syncService.sync(currentUserProvider.currentUserId());
    }

    @PatchMapping("/visibility")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateVisibility(@Valid @RequestBody UpdateGithubVisibilityRequest request) {
        connectionService.updateVisibility(request.visibleToFriends());
    }
}
