package com.astra.privacy.application.port.out;

import java.util.Map;
import java.util.UUID;

public interface ExportUserDataPort {

    Map<String, Object> export(UUID userId);
}
