package com.astra.social.application.port.in;

public interface SendFriendRequestUseCase {

    FriendshipResponse send(SendFriendRequestCommand command);
}
