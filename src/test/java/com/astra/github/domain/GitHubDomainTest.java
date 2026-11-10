package com.astra.github.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GitHubDomainTest {

    private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-10T12:00:00Z");
    private static final UUID USER = UUID.randomUUID();
    private static final GitHubProfile PROFILE = new GitHubProfile(1, "octocat", "https://avatar");

    private static GitHubConnection connectionWith(GitHubTokens tokens) {
        return GitHubConnection.withId(UUID.randomUUID(), USER, PROFILE, tokens, NOW, null, null, false);
    }

    private static GitHubTokens tokensExpiringAt(OffsetDateTime expiresAt, String refreshToken) {
        return new GitHubTokens("acesso", refreshToken, expiresAt, null, true);
    }

    private static SyncedRepository repo(String name, String language, int commits) {
        return new SyncedRepository(UUID.randomUUID(), USER, "octocat", name, "octocat/" + name, null, language,
                "https://github.com/octocat/" + name, false, 0, 0, null, commits);
    }

    @Test
    void tokenLongeDeVencerNaoRenova() {
        GitHubConnection connection = connectionWith(tokensExpiringAt(NOW.plusHours(1), "refresh"));

        assertThat(connection.accessToken(NOW, refresh -> {
            throw new AssertionError("não deveria renovar");
        })).isEqualTo("acesso");
    }

    @Test
    void tokenPrestesAVencerRenovaETrocaOsDois() {
        GitHubConnection connection = connectionWith(tokensExpiringAt(NOW.plusMinutes(1), "refresh"));

        String token = connection.accessToken(NOW, refresh -> new TokenGrant("novo", "refresh-novo", 28800, 3600L));

        assertThat(token).isEqualTo("novo");
        assertThat(connection.getTokens().refreshToken()).isEqualTo("refresh-novo");
        assertThat(connection.getTokens().accessExpiresAt()).isEqualTo(NOW.plusSeconds(28800));
        assertThat(connection.getTokens().refreshExpiresAt()).isEqualTo(NOW.plusSeconds(3600));
    }

    @Test
    void semRefreshTokenUsaOAcessoQueTem() {
        GitHubConnection connection = connectionWith(tokensExpiringAt(NOW.minusMinutes(1), null));

        assertThat(connection.accessToken(NOW, refresh -> null)).isEqualTo("acesso");
    }

    @Test
    void tokenIlegivelMarcaAConexaoComoInvalida() {
        GitHubConnection connection = connectionWith(GitHubTokens.unreadable(NOW, null));

        assertThatThrownBy(() -> connection.accessToken(NOW, refresh -> null))
                .isInstanceOf(InvalidGitHubConnectionException.class);
        assertThat(connection.getLastSyncError()).isEqualTo("Conexão inválida — reconecte sua conta do GitHub");
    }

    @Test
    void esperaDoisMinutosEntreSincronizacoesEReconectarLimpaOErro() {
        GitHubConnection connection = connectionWith(tokensExpiringAt(NOW.plusHours(1), null));
        connection.checkCanSync(NOW);
        connection.recordSync(NOW, "falhou");

        assertThatThrownBy(() -> connection.checkCanSync(NOW.plusMinutes(1))).isInstanceOf(SyncTooSoonException.class);
        connection.checkCanSync(NOW.plusMinutes(3));

        connection.reconnect(new GitHubProfile(1, "novo", null), tokensExpiringAt(NOW.plusHours(8), "r"));
        assertThat(connection.getLastSyncError()).isNull();
        assertThat(connection.getProfile().login()).isEqualTo("novo");
    }

    @Test
    void linguagensPorCommitsComPercentual() {
        List<LanguageShare> shares = LanguageShare.of(List.of(repo("a", "Java", 6), repo("b", "Java", 1),
                repo("c", "TypeScript", 1), repo("d", null, 5), repo("e", "Go", 0)));

        assertThat(shares).containsExactly(new LanguageShare("Java", 7, 87.5), new LanguageShare("TypeScript", 1, 12.5));
        assertThat(LanguageShare.of(List.of(repo("x", "Java", 0)))).isEmpty();
    }

    @Test
    void evidenciaCruzaPalavrasDoTituloComNomeOuLinguagemAteTres() {
        List<SyncedRepository> repos = List.of(repo("spring-api", "Java", 3), repo("java-notes", "Markdown", 1),
                repo("site", "TypeScript", 2), repo("parado", "Java", 0), repo("outro", "Java", 1),
                repo("mais", "Java", 1));

        assertThat(StepEvidence.matching("Fundamentos de Java", repos)).extracting(SyncedRepository::name)
                .containsExactly("spring-api", "java-notes", "outro");
        assertThat(StepEvidence.matching("Spring Boot", repos)).extracting(SyncedRepository::name)
                .containsExactly("spring-api");
        assertThat(StepEvidence.matching("Git", repos)).isEmpty();
    }

    @Test
    void periodosComecamNoDiaCerto() {
        java.time.LocalDate today = java.time.LocalDate.parse("2026-11-10");
        assertThat(ActivityPeriod.TODAY.since(today)).isEqualTo(today);
        assertThat(ActivityPeriod.QUARTER.since(today)).isEqualTo(today.minusDays(90));
    }
}
