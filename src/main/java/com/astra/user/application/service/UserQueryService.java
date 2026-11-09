package com.astra.user.application.service;

import com.astra.shared.exception.UnauthorizedException;
import com.astra.user.application.port.in.AdminUserResponse;
import com.astra.user.application.port.in.AuthInfo;
import com.astra.user.application.port.in.AvatarResponse;
import com.astra.user.application.port.in.ExportProfileQuery;
import com.astra.user.application.port.in.FindUserByHandleQuery;
import com.astra.user.application.port.in.GetAuthInfoQuery;
import com.astra.user.application.port.in.GetAvatarQuery;
import com.astra.user.application.port.in.GetUserQuery;
import com.astra.user.application.port.in.ListUsersQuery;
import com.astra.user.application.port.in.LoadUserSummariesQuery;
import com.astra.user.application.port.in.UserResponse;
import com.astra.user.application.port.in.UserSummary;
import com.astra.user.application.port.out.AvatarPort;
import com.astra.user.application.port.out.HandlePort;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.application.port.out.OAuthConnectionPort;
import com.astra.user.application.port.out.UserReadsPort;
import com.astra.user.domain.User;
import java.util.Base64;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class UserQueryService implements GetUserQuery, GetAuthInfoQuery, LoadUserSummariesQuery, FindUserByHandleQuery, ListUsersQuery, GetAvatarQuery, ExportProfileQuery {

    private final UserReadsPort userReadsPort;
    private final HandlePort handlePort;
    private final LoadUserPort loadUserPort;
    private final AvatarPort avatarPort;
    private final OAuthConnectionPort oAuthConnectionPort;

    UserQueryService(UserReadsPort userReadsPort, HandlePort handlePort, LoadUserPort loadUserPort, AvatarPort avatarPort, OAuthConnectionPort oAuthConnectionPort) {
        this.userReadsPort = userReadsPort;
        this.handlePort = handlePort;
        this.loadUserPort = loadUserPort;
        this.avatarPort = avatarPort;
        this.oAuthConnectionPort = oAuthConnectionPort;
    }

    @Override
    public UserResponse get(UUID userId) {
        return userReadsPort.loadResponse(userId).orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    @Override
    public Optional<AuthInfo> authInfo(UUID userId) {
        return userReadsPort.loadAuthInfo(userId);
    }

    @Override
    public Map<UUID, UserSummary> summaries(Collection<UUID> userIds) {
        return userIds.isEmpty() ? Map.of() : userReadsPort.loadSummaries(userIds);
    }

    @Override
    public Optional<UUID> findIdByHandle(String name, String tag) {
        return handlePort.findIdByHandle(name, tag);
    }

    @Override
    public List<AdminUserResponse> list() {
        return userReadsPort.listForAdmin();
    }

    @Override
    public Optional<AvatarResponse> avatar(UUID userId) {
        return avatarPort.load(userId).map(a -> new AvatarResponse(a.bytes(), a.contentType()));
    }

    @Override
    public Map<String, Object> exportProfile(UUID userId) {
        User user = OwnedUser.actor(loadUserPort, userId);
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("name", user.getName());
        profile.put("email", user.getEmail());
        profile.put("tag", user.getTag());
        profile.put("bio", user.getBio());
        profile.put("role", user.getRole().name());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("hasPassword", user.hasPassword());
        profile.put("termsAcceptedAt", user.getTermsAcceptedAt());
        profile.put("termsVersion", user.getTermsVersion());
        profile.put("loginProviders", oAuthConnectionPort.listByUser(userId).stream()
                .map(connection -> Map.of("provider", connection.provider(),
                        "connectedAt", connection.createdAt()))
                .toList());
        avatarPort.load(userId).ifPresent(avatar -> profile.put("avatar", Map.of(
                "contentType", avatar.contentType(),
                "base64", Base64.getEncoder().encodeToString(avatar.bytes()))));
        return profile;
    }
}
