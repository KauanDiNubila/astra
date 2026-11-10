package com.astra.github.domain;

import java.util.Arrays;
import java.util.List;

public final class StepEvidence {

    private static final int MAX_MATCHES = 3;
    private static final int MIN_WORD_LENGTH = 4;

    private StepEvidence() {
    }

    public static List<SyncedRepository> matching(String stepTitle, List<SyncedRepository> repositories) {
        List<String> words = Arrays.stream(stepTitle.toLowerCase().split("\\W+"))
                .filter(w -> w.length() >= MIN_WORD_LENGTH)
                .toList();
        return repositories.stream()
                .filter(r -> r.recentCommitCount() > 0 && matchesAnyWord(words, r))
                .limit(MAX_MATCHES)
                .toList();
    }

    private static boolean matchesAnyWord(List<String> words, SyncedRepository repository) {
        String language = repository.primaryLanguage() == null ? "" : repository.primaryLanguage().toLowerCase();
        String name = repository.name().toLowerCase();
        return words.stream().anyMatch(w -> name.contains(w)
                || (!language.isEmpty() && (language.contains(w) || w.contains(language))));
    }
}
