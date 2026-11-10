package com.astra.github.application.port.in;

import java.util.List;

public record StepEvidenceResponse(boolean connected, List<RepositoryInsight> matchedRepositories) {
}
