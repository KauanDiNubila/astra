package com.astra.roadmap;

import com.astra.github.GitHubInsightsService;
import com.astra.github.dto.GitHubStepEvidence;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.exception.NotFoundException;
import java.util.UUID;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/steps/{stepId}")
public class RoadmapStepGithubController {

    private final RoadmapStepRepository stepRepository;
    private final GitHubInsightsService insightsService;
    private final CurrentUserProvider currentUserProvider;

    public RoadmapStepGithubController(RoadmapStepRepository stepRepository, GitHubInsightsService insightsService,
            CurrentUserProvider currentUserProvider) {
        this.stepRepository = stepRepository;
        this.insightsService = insightsService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/github-evidence")
    @Transactional(readOnly = true)
    public GitHubStepEvidence evidence(@PathVariable UUID stepId) {
        UUID userId = currentUserProvider.currentUserId();
        RoadmapStep step = stepRepository.findById(stepId)
                .orElseThrow(() -> new NotFoundException("Step not found"));
        UUID owner = step.getRoadmap().getOwnerId();
        if (owner != null && !owner.equals(userId)) {
            throw new NotFoundException("Step not found");
        }
        return insightsService.evidenceForStepTitle(userId, step.getTitle());
    }
}
