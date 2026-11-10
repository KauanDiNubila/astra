package com.astra.privacy.application.port.in;

import java.util.Map;
import java.util.UUID;

public interface ExportUserDataQuery {

    Map<String, Object> export(UUID userId);
}
