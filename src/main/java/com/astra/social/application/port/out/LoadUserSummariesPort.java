package com.astra.social.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface LoadUserSummariesPort {

    Map<UUID, UserSummary> loadByIds(Collection<UUID> userIds);

    record UserSummary(UUID id, String name, String tag, String bio, boolean admin) {

        public String handle() {
            return name + "#" + tag;
        }
    }
}
