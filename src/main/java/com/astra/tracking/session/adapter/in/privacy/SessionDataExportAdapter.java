package com.astra.tracking.session.adapter.in.privacy;

import com.astra.tracking.session.application.port.in.ListSessionsQuery;
import com.astra.privacy.application.port.out.ExportUserDataPort;
import com.astra.shared.ExportRow;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3)
class SessionDataExportAdapter implements ExportUserDataPort {

    private final ListSessionsQuery listSessionsQuery;

    SessionDataExportAdapter(ListSessionsQuery listSessionsQuery) {
        this.listSessionsQuery = listSessionsQuery;
    }

    @Override
    public Map<String, Object> export(UUID userId) {
        return ExportRow.of("sessions", listSessionsQuery.listAll(userId));
    }
}
