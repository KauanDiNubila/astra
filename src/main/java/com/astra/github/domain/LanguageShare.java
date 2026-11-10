package com.astra.github.domain;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record LanguageShare(String language, int commitCount, double percentage) {

    public static List<LanguageShare> of(List<SyncedRepository> repositories) {
        Map<String, Integer> byLanguage = repositories.stream()
                .filter(r -> r.primaryLanguage() != null && r.recentCommitCount() > 0)
                .collect(Collectors.groupingBy(SyncedRepository::primaryLanguage,
                        Collectors.summingInt(SyncedRepository::recentCommitCount)));
        int total = byLanguage.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            return List.of();
        }
        return byLanguage.entrySet().stream()
                .map(e -> new LanguageShare(e.getKey(), e.getValue(), 100.0 * e.getValue() / total))
                .sorted(Comparator.comparingInt(LanguageShare::commitCount).reversed())
                .toList();
    }
}
