package com.astra.learning.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record LinkGithubRepositoryRequest(@NotNull UUID repositoryId) {
}
