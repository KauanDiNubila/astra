package com.astra.chat.adapter.out.github;

import com.astra.chat.application.port.out.LoadGithubProfilesPort;
import com.astra.github.entity.GitHubConnection;
import com.astra.github.repository.GitHubConnectionRepository;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class GithubProfilesAdapter implements LoadGithubProfilesPort {

    private final GitHubConnectionRepository gitHubConnectionRepository;

    GithubProfilesAdapter(GitHubConnectionRepository gitHubConnectionRepository) {
        this.gitHubConnectionRepository = gitHubConnectionRepository;
    }

    @Override
    public Map<UUID, GithubProfile> visibleToFriends(Collection<UUID> userIds) {
        return gitHubConnectionRepository.findByUserIdIn(userIds).stream()
                .filter(GitHubConnection::isVisibleToFriends)
                .collect(Collectors.toMap(GitHubConnection::getUserId,
                        c -> new GithubProfile(c.getGithubLogin(), c.getGithubAvatarUrl())));
    }
}
