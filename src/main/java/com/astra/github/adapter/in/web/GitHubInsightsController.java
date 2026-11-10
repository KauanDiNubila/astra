package com.astra.github.adapter.in.web;

import com.astra.github.application.port.in.GetGitHubActivityQuery;
import com.astra.github.application.port.in.GetGitHubInsightsQuery;
import com.astra.github.application.port.in.GitHubActivityResponse;
import com.astra.github.application.port.in.GitHubInsightsResponse;
import com.astra.shared.CurrentUserProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/github")
class GitHubInsightsController {

    private final GetGitHubActivityQuery getGitHubActivityQuery;
    private final GetGitHubInsightsQuery getGitHubInsightsQuery;
    private final CurrentUserProvider currentUserProvider;

    GitHubInsightsController(GetGitHubActivityQuery getGitHubActivityQuery,
                             GetGitHubInsightsQuery getGitHubInsightsQuery, CurrentUserProvider currentUserProvider) {
        this.getGitHubActivityQuery = getGitHubActivityQuery;
        this.getGitHubInsightsQuery = getGitHubInsightsQuery;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/activity")
    GitHubActivityResponse activity() {
        return getGitHubActivityQuery.activity(currentUserProvider.currentUserId());
    }

    @GetMapping("/insights")
    GitHubInsightsResponse insights() {
        return getGitHubInsightsQuery.insights(currentUserProvider.currentUserId());
    }
}
