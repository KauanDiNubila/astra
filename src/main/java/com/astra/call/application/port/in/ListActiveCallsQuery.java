package com.astra.call.application.port.in;

import java.util.List;
import java.util.UUID;

public interface ListActiveCallsQuery {

    List<ActiveCallResponse> activeFor(UUID userId);
}
