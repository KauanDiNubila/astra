package com.astra.social.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListFriendshipsQuery {

    List<FriendshipResponse> friends(UUID userId);

    List<FriendshipResponse> pendingRequests(UUID userId);
}
