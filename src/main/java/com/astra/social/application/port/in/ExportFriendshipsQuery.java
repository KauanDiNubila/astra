package com.astra.social.application.port.in;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ExportFriendshipsQuery {

    List<FriendshipExport> export(UUID userId);

    record FriendshipExport(UUID userId, String name, String status, boolean sentByMe, OffsetDateTime createdAt) {
    }
}
