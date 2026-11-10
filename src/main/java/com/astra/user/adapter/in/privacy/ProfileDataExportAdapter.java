package com.astra.user.adapter.in.privacy;

import com.astra.user.application.port.in.ExportProfileQuery;
import com.astra.privacy.application.port.out.ExportUserDataPort;
import com.astra.shared.ExportRow;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
class ProfileDataExportAdapter implements ExportUserDataPort {

    private final ExportProfileQuery exportProfileQuery;

    ProfileDataExportAdapter(ExportProfileQuery exportProfileQuery) {
        this.exportProfileQuery = exportProfileQuery;
    }

    @Override
    public Map<String, Object> export(UUID userId) {
        return ExportRow.of("profile", exportProfileQuery.exportProfile(userId));
    }
}
