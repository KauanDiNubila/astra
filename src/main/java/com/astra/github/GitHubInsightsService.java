package com.astra.github;

import com.astra.github.dto.GitHubActivityResponse;
import com.astra.github.dto.GitHubDailyPoint;
import com.astra.github.dto.GitHubInsightsResponse;
import com.astra.github.dto.GitHubLanguageShare;
import com.astra.github.dto.GitHubPeriodSummary;
import com.astra.github.dto.GitHubRepositoryInsight;
import com.astra.github.dto.GitHubStepEvidence;
import com.astra.shared.CurrentUserProvider;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GitHubInsightsService {

    private final GitHubConnectionRepository connectionRepository;
    private final GitHubRepositoryRepository repositoryRepository;
    private final GitHubDailyActivityRepository dailyActivityRepository;
    private final GitHubActivitySummaryRepository activitySummaryRepository;
    private final CurrentUserProvider currentUserProvider;

    public GitHubInsightsService(GitHubConnectionRepository connectionRepository,
            GitHubRepositoryRepository repositoryRepository,
            GitHubDailyActivityRepository dailyActivityRepository,
            GitHubActivitySummaryRepository activitySummaryRepository, CurrentUserProvider currentUserProvider) {
        this.connectionRepository = connectionRepository;
        this.repositoryRepository = repositoryRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.activitySummaryRepository = activitySummaryRepository;
        this.currentUserProvider = currentUserProvider;
    }

    @Transactional(readOnly = true)
    public GitHubActivityResponse activity() {
        UUID userId = currentUserProvider.currentUserId();
        GitHubConnection connection = connectionRepository.findByUserId(userId).orElse(null);
        if (connection == null) {
            return new GitHubActivityResponse(false, List.of(), null, null);
        }
        List<GitHubPeriodSummary> periods = activitySummaryRepository.findByUserId(userId).stream()
                .map(s -> new GitHubPeriodSummary(s.getPeriod(), s.getCommitCount(), s.getPullRequestOpenedCount(),
                        s.getPullRequestMergedCount(), s.getIssueClosedCount(), s.getActiveRepoCount()))
                .sorted(Comparator.comparing(GitHubPeriodSummary::period))
                .toList();
        return new GitHubActivityResponse(true, periods, connection.getLastSyncedAt(), connection.getLastSyncError());
    }

    @Transactional(readOnly = true)
    public GitHubInsightsResponse insights() {
        UUID userId = currentUserProvider.currentUserId();
        GitHubConnection connection = connectionRepository.findByUserId(userId).orElse(null);
        if (connection == null) {
            return new GitHubInsightsResponse(false, List.of(), List.of(), List.of(), null, null);
        }

        List<GitHubRepository> repositories = repositoryRepository.findByUserIdOrderByRecentCommitCountDesc(userId);

        List<GitHubRepositoryInsight> repoInsights = repositories.stream()
                .map(r -> new GitHubRepositoryInsight(r.getId(), r.getName(), r.getFullName(), r.getHtmlUrl(),
                        r.getPrimaryLanguage(), r.getRecentCommitCount(), r.isPrivate()))
                .toList();

        List<GitHubLanguageShare> languages = languageBreakdown(repositories);

        // Ano inteiro (mesma janela que o heatmap combinado do dashboard usa) -
        // o front recorta os ultimos 90 dias sozinho pra exibir no grafico.
        LocalDate since = LocalDate.now(ZoneOffset.UTC).minusDays(364);
        List<GitHubDailyPoint> series = dailyActivityRepository
                .findByUserIdAndActivityDateGreaterThanEqualOrderByActivityDate(userId, since).stream()
                .map(a -> new GitHubDailyPoint(a.getActivityDate(), a.getContributionCount()))
                .toList();

        return new GitHubInsightsResponse(true, repoInsights, languages, series,
                connection.getLastSyncedAt(), connection.getLastSyncError());
    }

    // Puramente local (nenhuma chamada ao GitHub): so cruza o titulo do step
    // com o que ja foi sincronizado. Sugestao de baixa confianca - o
    // consumidor (front) deve apresentar isso como "possivel evidencia", nao
    // como prova.
    @Transactional(readOnly = true)
    public GitHubStepEvidence evidenceForStepTitle(UUID userId, String stepTitle) {
        GitHubConnection connection = connectionRepository.findByUserId(userId).orElse(null);
        if (connection == null) {
            return new GitHubStepEvidence(false, List.of());
        }
        List<String> titleWords = Arrays.stream(stepTitle.toLowerCase().split("\\W+"))
                .filter(w -> w.length() > 3)
                .toList();
        List<GitHubRepositoryInsight> matches = repositoryRepository
                .findByUserIdOrderByRecentCommitCountDesc(userId).stream()
                .filter(r -> r.getRecentCommitCount() > 0 && matchesAnyWord(titleWords, r))
                .map(r -> new GitHubRepositoryInsight(r.getId(), r.getName(), r.getFullName(), r.getHtmlUrl(),
                        r.getPrimaryLanguage(), r.getRecentCommitCount(), r.isPrivate()))
                .limit(3)
                .toList();
        return new GitHubStepEvidence(true, matches);
    }

    private boolean matchesAnyWord(List<String> words, GitHubRepository repository) {
        String language = repository.getPrimaryLanguage() == null ? "" : repository.getPrimaryLanguage().toLowerCase();
        String name = repository.getName().toLowerCase();
        return words.stream().anyMatch(w -> name.contains(w)
                || (!language.isEmpty() && (language.contains(w) || w.contains(language))));
    }

    private List<GitHubLanguageShare> languageBreakdown(List<GitHubRepository> repositories) {
        Map<String, Integer> byLanguage = repositories.stream()
                .filter(r -> r.getPrimaryLanguage() != null && r.getRecentCommitCount() > 0)
                .collect(Collectors.groupingBy(GitHubRepository::getPrimaryLanguage,
                        Collectors.summingInt(GitHubRepository::getRecentCommitCount)));
        int total = byLanguage.values().stream().mapToInt(Integer::intValue).sum();
        if (total == 0) {
            return List.of();
        }
        return byLanguage.entrySet().stream()
                .map(e -> new GitHubLanguageShare(e.getKey(), e.getValue(), 100.0 * e.getValue() / total))
                .sorted(Comparator.comparingInt(GitHubLanguageShare::commitCount).reversed())
                .toList();
    }
}
