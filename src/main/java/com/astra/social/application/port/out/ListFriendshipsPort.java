package com.astra.social.application.port.out;

import com.astra.social.domain.Friendship;
import java.util.List;
import java.util.UUID;

public interface ListFriendshipsPort {

    List<Friendship> listAccepted(UUID userId);

    List<Friendship> listPending(UUID userId);

    List<Friendship> listAll(UUID userId);
}
