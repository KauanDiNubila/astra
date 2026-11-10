package com.astra.chat.adapter.out.github;

import com.astra.chat.application.port.out.LoadGithubProfilesPort;
import com.astra.github.application.port.in.ListVisibleProfilesQuery;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class GithubProfilesAdapter implements LoadGithubProfilesPort {

    private final ListVisibleProfilesQuery listVisibleProfilesQuery;

    GithubProfilesAdapter(ListVisibleProfilesQuery listVisibleProfilesQuery) {
        this.listVisibleProfilesQuery = listVisibleProfilesQuery;
    }

    @Override
    public Map<UUID, GithubProfile> visibleToFriends(Collection<UUID> userIds) {
        return listVisibleProfilesQuery.visibleToFriends(userIds).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> new GithubProfile(e.getValue().login(), e.getValue().avatarUrl())));
    }
}
