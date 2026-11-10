package com.astra.github.application.port.in;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface ListVisibleProfilesQuery {

    Map<UUID, VisibleProfile> visibleToFriends(Collection<UUID> userIds);
}
