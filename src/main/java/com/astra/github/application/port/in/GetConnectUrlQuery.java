package com.astra.github.application.port.in;

import java.util.UUID;

public interface GetConnectUrlQuery {

    String connectUrl(UUID userId);
}
