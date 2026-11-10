package com.astra.github.application.port.in;

import java.util.UUID;

public interface GetGitHubInsightsQuery {

    GitHubInsightsResponse insights(UUID userId);
}
