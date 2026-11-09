package com.astra.social.application.port.in;

import java.util.List;
import java.util.UUID;

public interface GetFriendIdsQuery {

    List<UUID> friendIdsOf(UUID userId);
}
