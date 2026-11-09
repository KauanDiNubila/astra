package com.astra.social.application.port.out;

import com.astra.social.domain.Friendship;

public interface UpdateFriendshipStatusPort {

    void updateStatus(Friendship friendship);
}
