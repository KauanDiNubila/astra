package com.astra.github.application.port.in;

import com.astra.github.domain.SyncedRepository;
import java.util.UUID;

public record RepositoryInsight(UUID id, String name, String fullName, String htmlUrl, String primaryLanguage,
                                int recentCommitCount, boolean isPrivate) {

    public static RepositoryInsight from(SyncedRepository r) {
        return new RepositoryInsight(r.id(), r.name(), r.fullName(), r.htmlUrl(), r.primaryLanguage(),
                r.recentCommitCount(), r.isPrivate());
    }
}
