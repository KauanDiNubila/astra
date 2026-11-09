package com.astra.user.application.port.in;

import java.util.Map;
import java.util.UUID;

public interface ExportProfileQuery {

    Map<String, Object> exportProfile(UUID userId);
}
