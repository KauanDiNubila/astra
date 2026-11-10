package com.astra.privacy.service;

import com.astra.chat.application.port.in.ExportGroupsQuery;
import com.astra.chat.application.port.in.ExportMessagesQuery;
import com.astra.github.application.port.in.ExportGitHubQuery;
import com.astra.learning.application.port.in.ExportCoursesQuery;
import com.astra.learning.application.port.in.ExportGoalsQuery;
import com.astra.roadmap.application.port.in.ExportCoursePinsQuery;
import com.astra.roadmap.application.port.in.ExportRoadmapsQuery;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.ExportRow;
import com.astra.social.application.port.in.ExportFriendshipsQuery;
import com.astra.tracking.category.application.port.in.ListCategoriesQuery;
import com.astra.tracking.session.application.port.in.ListSessionsQuery;
import com.astra.user.application.port.in.ExportProfileQuery;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataExportService {

    private final CurrentUserProvider currentUserProvider;
    private final ExportProfileQuery exportProfileQuery;
    private final ListCategoriesQuery listCategoriesQuery;
    private final ListSessionsQuery listSessionsQuery;
    private final ExportCoursesQuery exportCoursesQuery;
    private final ExportGoalsQuery exportGoalsQuery;
    private final ExportRoadmapsQuery exportRoadmapsQuery;
    private final ExportCoursePinsQuery exportCoursePinsQuery;
    private final ExportFriendshipsQuery exportFriendshipsQuery;
    private final ExportMessagesQuery exportMessagesQuery;
    private final ExportGroupsQuery exportGroupsQuery;
    private final ExportGitHubQuery exportGitHubQuery;

    public DataExportService(CurrentUserProvider currentUserProvider, ExportProfileQuery exportProfileQuery,
            ListCategoriesQuery listCategoriesQuery, ListSessionsQuery listSessionsQuery, ExportCoursesQuery exportCoursesQuery,
            ExportGoalsQuery exportGoalsQuery, ExportRoadmapsQuery exportRoadmapsQuery,
            ExportCoursePinsQuery exportCoursePinsQuery,
            ExportFriendshipsQuery exportFriendshipsQuery, ExportMessagesQuery exportMessagesQuery,
            ExportGroupsQuery exportGroupsQuery,
            ExportGitHubQuery exportGitHubQuery) {
        this.currentUserProvider = currentUserProvider;
        this.exportProfileQuery = exportProfileQuery;
        this.listCategoriesQuery = listCategoriesQuery;
        this.listSessionsQuery = listSessionsQuery;
        this.exportCoursesQuery = exportCoursesQuery;
        this.exportGoalsQuery = exportGoalsQuery;
        this.exportRoadmapsQuery = exportRoadmapsQuery;
        this.exportCoursePinsQuery = exportCoursePinsQuery;
        this.exportFriendshipsQuery = exportFriendshipsQuery;
        this.exportMessagesQuery = exportMessagesQuery;
        this.exportGroupsQuery = exportGroupsQuery;
        this.exportGitHubQuery = exportGitHubQuery;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportCurrentUser() {
        UUID userId = currentUserProvider.currentUserId();
        return ExportRow.of(
                "exportedAt", OffsetDateTime.now(),
                "profile", exportProfileQuery.exportProfile(userId),
                "categories", listCategoriesQuery.list(userId),
                "sessions", listSessionsQuery.listAll(userId),
                "courses", exportCoursesQuery.exportCourses(userId),
                "goals", exportGoalsQuery.exportGoals(userId),
                "roadmaps", exportRoadmapsQuery.exportRoadmaps(userId),
                "coursePins", exportCoursePinsQuery.exportPins(userId),
                "friendships", exportFriendshipsQuery.export(userId),
                "groups", exportGroupsQuery.exportGroups(userId),
                "messages", exportMessagesQuery.exportMessages(userId),
                "github", exportGitHubQuery.exportGitHub(userId));
    }
}
