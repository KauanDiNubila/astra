package com.astra.github.application.service;

import com.astra.github.application.port.in.SyncGitHubUseCase;
import com.astra.github.application.port.out.LoadConnectionPort;
import com.astra.github.application.port.out.LoadContributionsPort;
import com.astra.github.application.port.out.RecordSyncedDataPort;
import com.astra.github.application.port.out.UpdateConnectionPort;
import com.astra.github.domain.ActivityPeriod;
import com.astra.github.domain.GitHubApiException;
import com.astra.github.domain.GitHubConnection;
import com.astra.github.domain.YearSnapshot;
import com.astra.shared.exception.ConflictException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SyncGitHubService implements SyncGitHubUseCase {

    private static final List<ActivityPeriod> SHORTER_PERIODS =
            List.of(ActivityPeriod.WEEK, ActivityPeriod.MONTH, ActivityPeriod.QUARTER, ActivityPeriod.TODAY);

    private final LoadConnectionPort loadConnectionPort;
    private final UpdateConnectionPort updateConnectionPort;
    private final AccessTokens accessTokens;
    private final LoadContributionsPort contributionsPort;
    private final RecordSyncedDataPort recordSyncedDataPort;
    private final Clock clock;

    SyncGitHubService(LoadConnectionPort loadConnectionPort, UpdateConnectionPort updateConnectionPort,
                      AccessTokens accessTokens, LoadContributionsPort contributionsPort,
                      RecordSyncedDataPort recordSyncedDataPort, Clock clock) {
        this.loadConnectionPort = loadConnectionPort;
        this.updateConnectionPort = updateConnectionPort;
        this.accessTokens = accessTokens;
        this.contributionsPort = contributionsPort;
        this.recordSyncedDataPort = recordSyncedDataPort;
        this.clock = clock;
    }

    @Override
    @Transactional(noRollbackFor = {GitHubApiException.class, ConflictException.class})
    public void sync(UUID userId) {
        GitHubConnection connection = Connections.require(loadConnectionPort, userId);
        connection.checkCanSync(OffsetDateTime.now(clock));
        String login = connection.getProfile().login();
        String accessToken = accessTokens.validFor(connection);
        try {
            LocalDate today = LocalDate.now(clock.withZone(ZoneOffset.UTC));
            YearSnapshot year = contributionsPort.year(accessToken, login, ActivityPeriod.YEAR.since(today), today);
            recordSyncedDataPort.recordDaily(userId, year.daily());
            recordSyncedDataPort.recordRepositories(userId, year.repositories(), OffsetDateTime.now(clock));
            recordSyncedDataPort.recordSummary(userId, year.summary());
            for (ActivityPeriod period : SHORTER_PERIODS) {
                recordSyncedDataPort.recordSummary(userId,
                        contributionsPort.period(accessToken, login, period, period.since(today), today));
            }
            connection.recordSync(OffsetDateTime.now(clock), null);
        } catch (GitHubApiException e) {
            connection.recordSync(OffsetDateTime.now(clock), e.getMessage());
            throw e;
        } finally {
            updateConnectionPort.update(connection);
        }
    }
}
