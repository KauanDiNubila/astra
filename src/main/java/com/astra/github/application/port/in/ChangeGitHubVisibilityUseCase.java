package com.astra.github.application.port.in;

import java.util.UUID;

public interface ChangeGitHubVisibilityUseCase {

    void change(UUID userId, boolean visibleToFriends);
}
