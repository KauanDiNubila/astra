package com.astra.tracking.session.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record LinkRepositoryRequest(@NotNull UUID repositoryId) {
}
