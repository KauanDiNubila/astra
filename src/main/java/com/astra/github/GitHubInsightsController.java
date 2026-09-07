package com.astra.github;

import com.astra.github.dto.GitHubActivityResponse;
import com.astra.github.dto.GitHubInsightsResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubInsightsController {

    private final GitHubInsightsService insightsService;

    public GitHubInsightsController(GitHubInsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/github/activity")
    public GitHubActivityResponse activity() {
        return insightsService.activity();
    }

    @GetMapping("/github/insights")
    public GitHubInsightsResponse insights() {
        return insightsService.insights();
    }
}
