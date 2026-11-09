package com.astra.stats.application.port.in;

import java.util.UUID;

public interface GetDashboardQuery {

    DashboardResponse dashboard(UUID userId);
}
