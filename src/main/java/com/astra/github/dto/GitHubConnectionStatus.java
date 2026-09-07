package com.astra.github.dto;

import java.time.OffsetDateTime;

public record GitHubConnectionStatus(
        boolean connected,
        String login,
        String avatarUrl,
        OffsetDateTime connectedAt,
        OffsetDateTime lastSyncedAt,
        String lastSyncError,
        boolean visibleToFriends) {

    public static GitHubConnectionStatus disconnected() {
        return new GitHubConnectionStatus(false, null, null, null, null, null, false);
    }
}
