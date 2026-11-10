package com.astra.chat.application.service;

import com.astra.chat.application.port.out.LoadUserCardsPort;
import com.astra.chat.application.port.out.LoadUserCardsPort.UserCard;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

final class MemberNames {

    private final Map<UUID, UserCard> cards;

    private MemberNames(Map<UUID, UserCard> cards) {
        this.cards = cards;
    }

    static MemberNames of(LoadUserCardsPort loadUserCardsPort, Collection<UUID> userIds) {
        return new MemberNames(loadUserCardsPort.cards(userIds));
    }

    String nameOf(UUID userId) {
        UserCard card = cards.get(userId);
        return card != null ? card.name() : "";
    }
}
