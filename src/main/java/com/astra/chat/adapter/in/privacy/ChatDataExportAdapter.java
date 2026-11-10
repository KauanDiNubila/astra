package com.astra.chat.adapter.in.privacy;

import com.astra.chat.application.port.in.ExportGroupsQuery;
import com.astra.chat.application.port.in.ExportMessagesQuery;
import com.astra.privacy.application.port.out.ExportUserDataPort;
import com.astra.shared.ExportRow;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(7)
class ChatDataExportAdapter implements ExportUserDataPort {

    private final ExportGroupsQuery exportGroupsQuery;
    private final ExportMessagesQuery exportMessagesQuery;

    ChatDataExportAdapter(ExportGroupsQuery exportGroupsQuery, ExportMessagesQuery exportMessagesQuery) {
        this.exportGroupsQuery = exportGroupsQuery;
        this.exportMessagesQuery = exportMessagesQuery;
    }

    @Override
    public Map<String, Object> export(UUID userId) {
        return ExportRow.of("groups", exportGroupsQuery.exportGroups(userId),
                "messages", exportMessagesQuery.exportMessages(userId));
    }
}
