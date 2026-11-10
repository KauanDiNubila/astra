package com.astra.user.application.port.in;

import java.util.UUID;

public interface IssueRefreshTokenUseCase {

    String issue(UUID userId);
}
