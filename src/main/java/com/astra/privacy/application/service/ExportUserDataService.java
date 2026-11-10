package com.astra.privacy.application.service;

import com.astra.privacy.application.port.in.ExportUserDataQuery;
import com.astra.privacy.application.port.out.ExportUserDataPort;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ExportUserDataService implements ExportUserDataQuery {

    private final List<ExportUserDataPort> exportUserDataPorts;
    private final Clock clock;

    ExportUserDataService(List<ExportUserDataPort> exportUserDataPorts, Clock clock) {
        this.exportUserDataPorts = exportUserDataPorts;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> export(UUID userId) {
        Map<String, Object> export = new LinkedHashMap<>();
        export.put("exportedAt", OffsetDateTime.now(clock));
        exportUserDataPorts.forEach(port -> export.putAll(port.export(userId)));
        return export;
    }
}
