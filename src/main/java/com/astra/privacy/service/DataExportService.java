package com.astra.privacy.service;

import com.astra.chat.service.ChatGroupService;
import com.astra.chat.service.ChatService;
import com.astra.github.service.GitHubConnectionService;
import com.astra.learning.service.CourseService;
import com.astra.learning.service.GoalService;
import com.astra.roadmap.service.PinService;
import com.astra.roadmap.service.RoadmapService;
import com.astra.shared.CurrentUserProvider;
import com.astra.shared.ExportRow;
import com.astra.social.service.FriendshipService;
import com.astra.tracking.category.service.CategoryService;
import com.astra.tracking.session.service.SessionService;
import com.astra.user.service.UserService;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DataExportService {

    private final CurrentUserProvider currentUserProvider;
    private final UserService userService;
    private final CategoryService categoryService;
    private final SessionService sessionService;
    private final CourseService courseService;
    private final GoalService goalService;
    private final RoadmapService roadmapService;
    private final PinService pinService;
    private final FriendshipService friendshipService;
    private final ChatService chatService;
    private final ChatGroupService chatGroupService;
    private final GitHubConnectionService gitHubConnectionService;

    public DataExportService(CurrentUserProvider currentUserProvider, UserService userService,
            CategoryService categoryService, SessionService sessionService, CourseService courseService,
            GoalService goalService, RoadmapService roadmapService, PinService pinService,
            FriendshipService friendshipService, ChatService chatService, ChatGroupService chatGroupService,
            GitHubConnectionService gitHubConnectionService) {
        this.currentUserProvider = currentUserProvider;
        this.userService = userService;
        this.categoryService = categoryService;
        this.sessionService = sessionService;
        this.courseService = courseService;
        this.goalService = goalService;
        this.roadmapService = roadmapService;
        this.pinService = pinService;
        this.friendshipService = friendshipService;
        this.chatService = chatService;
        this.chatGroupService = chatGroupService;
        this.gitHubConnectionService = gitHubConnectionService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportCurrentUser() {
        UUID userId = currentUserProvider.currentUserId();
        return ExportRow.of(
                "exportedAt", OffsetDateTime.now(),
                "profile", userService.exportData(userId),
                "categories", categoryService.exportData(userId),
                "sessions", sessionService.exportData(userId),
                "courses", courseService.exportData(userId),
                "goals", goalService.exportData(userId),
                "roadmaps", roadmapService.exportData(userId),
                "coursePins", pinService.exportData(userId),
                "friendships", friendshipService.exportData(userId),
                "groups", chatGroupService.exportData(userId),
                "messages", chatService.exportData(userId),
                "github", gitHubConnectionService.exportData(userId));
    }
}
