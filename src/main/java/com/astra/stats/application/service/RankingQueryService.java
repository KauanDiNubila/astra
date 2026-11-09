package com.astra.stats.application.service;

import com.astra.stats.application.port.in.GetRankingQuery;
import com.astra.stats.application.port.in.RankingEntry;
import com.astra.stats.application.port.in.RankingScope;
import com.astra.stats.application.port.out.LoadFriendIdsPort;
import com.astra.stats.application.port.out.LoadMinutesByUserPort;
import com.astra.stats.application.port.out.LoadMinutesByUserPort.UserMinutes;
import com.astra.stats.application.port.out.LoadRankedUsersPort;
import com.astra.stats.application.port.out.LoadRankedUsersPort.RankedUser;
import com.astra.stats.domain.RankingPeriod;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class RankingQueryService implements GetRankingQuery {

    private static final RankedUser UNKNOWN = new RankedUser("", false);

    private final LoadMinutesByUserPort loadMinutesByUserPort;
    private final LoadFriendIdsPort loadFriendIdsPort;
    private final LoadRankedUsersPort loadRankedUsersPort;

    RankingQueryService(LoadMinutesByUserPort loadMinutesByUserPort, LoadFriendIdsPort loadFriendIdsPort,
                   LoadRankedUsersPort loadRankedUsersPort) {
        this.loadMinutesByUserPort = loadMinutesByUserPort;
        this.loadFriendIdsPort = loadFriendIdsPort;
        this.loadRankedUsersPort = loadRankedUsersPort;
    }

    @Override
    public List<RankingEntry> ranking(UUID userId, RankingPeriod period, RankingScope scope) {
        OffsetDateTime start = StatsCalendar.startOf(period.startOn(StatsCalendar.today()));
        List<UserMinutes> rows = scope == RankingScope.FRIENDS
                ? loadMinutesByUserPort.sinceForUsers(start, friendsAndMe(userId))
                : loadMinutesByUserPort.since(start);
        Map<UUID, RankedUser> users = loadRankedUsersPort.loadByIds(rows.stream().map(UserMinutes::userId).toList());

        List<RankingEntry> entries = new ArrayList<>();
        int position = 1;
        for (UserMinutes row : rows) {
            RankedUser user = users.getOrDefault(row.userId(), UNKNOWN);
            entries.add(new RankingEntry(position++, row.userId(), user.name(), row.minutes(), user.admin()));
        }
        return entries;
    }

    private Set<UUID> friendsAndMe(UUID userId) {
        Set<UUID> ids = new LinkedHashSet<>(loadFriendIdsPort.friendIdsOf(userId));
        ids.add(userId);
        return ids;
    }
}
