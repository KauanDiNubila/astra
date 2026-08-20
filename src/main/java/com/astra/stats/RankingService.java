package com.astra.stats;

import com.astra.shared.CurrentUserProvider;
import com.astra.social.FriendshipService;
import com.astra.stats.dto.RankingEntry;
import com.astra.tracking.session.SessionStatsService;
import com.astra.tracking.session.UserMinutes;
import com.astra.user.UserService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class RankingService {

    private static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");

    private final SessionStatsService sessionStatsService;
    private final UserService userService;
    private final FriendshipService friendshipService;
    private final CurrentUserProvider currentUserProvider;

    public RankingService(SessionStatsService sessionStatsService, UserService userService,
                          FriendshipService friendshipService, CurrentUserProvider currentUserProvider) {
        this.sessionStatsService = sessionStatsService;
        this.userService = userService;
        this.friendshipService = friendshipService;
        this.currentUserProvider = currentUserProvider;
    }

    public List<RankingEntry> ranking(RankingPeriod period, RankingScope scope) {
        OffsetDateTime start = startOf(period);
        List<UserMinutes> rows = scope == RankingScope.FRIENDS
                ? friendsRanking(start)
                : sessionStatsService.rankingSince(start);
        Map<UUID, String> names = userService.namesByIds(rows.stream().map(UserMinutes::userId).toList());

        List<RankingEntry> entries = new ArrayList<>();
        int position = 1;
        for (UserMinutes row : rows) {
            entries.add(new RankingEntry(position++, row.userId(),
                    names.getOrDefault(row.userId(), ""), row.minutes()));
        }
        return entries;
    }

    private List<UserMinutes> friendsRanking(OffsetDateTime start) {
        UUID me = currentUserProvider.currentUserId();
        Set<UUID> ids = new LinkedHashSet<>(friendshipService.friendIdsOf(me));
        ids.add(me);
        return sessionStatsService.rankingSinceForUsers(start, ids);
    }

    private OffsetDateTime startOf(RankingPeriod period) {
        LocalDate today = LocalDate.now(ZONE);
        LocalDate startDate = switch (period) {
            case DAILY -> today;
            case WEEKLY -> today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            case MONTHLY -> today.withDayOfMonth(1);
        };
        return startDate.atStartOfDay(ZONE).toOffsetDateTime();
    }
}
