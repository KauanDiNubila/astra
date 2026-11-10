package com.astra.github.application.port.in;

import com.astra.github.domain.CommitInfo;
import java.util.List;
import java.util.UUID;

public record SessionActivityResponse(boolean connected, List<CommitInfo> commits, UUID suggestedRepositoryId,
                                      String suggestedRepositoryName) {
}
