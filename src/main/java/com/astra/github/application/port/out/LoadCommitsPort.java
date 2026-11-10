package com.astra.github.application.port.out;

import com.astra.github.domain.CommitInfo;
import com.astra.github.domain.SyncedRepository;
import java.time.OffsetDateTime;
import java.util.List;

public interface LoadCommitsPort {

    List<CommitInfo> commits(String accessToken, SyncedRepository repository, OffsetDateTime from, OffsetDateTime to);
}
