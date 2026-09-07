package com.astra.github.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkGithubRepositoryRequest(@NotNull UUID repositoryId) {
}
