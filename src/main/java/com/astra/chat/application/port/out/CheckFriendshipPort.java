package com.astra.chat.application.port.out;

import java.util.UUID;

public interface CheckFriendshipPort {

    boolean areFriends(UUID a, UUID b);
}
