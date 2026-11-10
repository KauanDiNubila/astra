package com.astra.github.application.port.in;

import java.util.Map;
import java.util.UUID;

public interface ExportGitHubQuery {

    Map<String, Object> exportGitHub(UUID userId);
}
