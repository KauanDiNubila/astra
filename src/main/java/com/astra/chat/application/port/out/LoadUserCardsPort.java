package com.astra.chat.application.port.out;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface LoadUserCardsPort {

    Map<UUID, UserCard> cards(Collection<UUID> userIds);

    record UserCard(String name, String tag, String bio, boolean staff) {
    }
}
