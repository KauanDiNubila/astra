package com.astra.chat.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface LoadGithubProfilesPort {

    Map<UUID, GithubProfile> visibleToFriends(Collection<UUID> userIds);

    record GithubProfile(String login, String avatarUrl) {
    }
}
