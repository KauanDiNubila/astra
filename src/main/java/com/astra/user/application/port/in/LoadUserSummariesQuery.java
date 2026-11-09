package com.astra.user.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface LoadUserSummariesQuery {

    Map<UUID, UserSummary> summaries(Collection<UUID> userIds);
}
