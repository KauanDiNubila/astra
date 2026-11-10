package com.astra.chat.application.port.in;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ExportGroupsQuery {

    List<Map<String, Object>> exportGroups(UUID userId);
}
