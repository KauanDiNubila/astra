package com.astra.user.application.port.out;

import com.astra.user.application.port.in.AdminUserResponse;
import com.astra.user.application.port.in.AuthInfo;
import com.astra.user.application.port.in.UserResponse;
import com.astra.user.application.port.in.UserSummary;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public interface UserReadsPort {

    Optional<UserResponse> loadResponse(UUID userId);

    Optional<AuthInfo> loadAuthInfo(UUID userId);

    Map<UUID, UserSummary> loadSummaries(Collection<UUID> userIds);

    List<AdminUserResponse> listForAdmin();
}
