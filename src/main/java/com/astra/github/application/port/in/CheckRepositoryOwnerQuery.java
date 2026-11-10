package com.astra.github.application.port.in;

import java.util.UUID;

public interface CheckRepositoryOwnerQuery {

    boolean isOwner(UUID repositoryId, UUID userId);
}
