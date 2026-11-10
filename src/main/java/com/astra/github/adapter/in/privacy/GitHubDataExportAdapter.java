package com.astra.github.adapter.in.privacy;

import com.astra.github.application.port.in.ExportGitHubQuery;
import com.astra.privacy.application.port.out.ExportUserDataPort;
import com.astra.shared.ExportRow;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(8)
class GitHubDataExportAdapter implements ExportUserDataPort {

    private final ExportGitHubQuery exportGitHubQuery;

    GitHubDataExportAdapter(ExportGitHubQuery exportGitHubQuery) {
        this.exportGitHubQuery = exportGitHubQuery;
    }

    @Override
    public Map<String, Object> export(UUID userId) {
        return ExportRow.of("github", exportGitHubQuery.exportGitHub(userId));
    }
}
