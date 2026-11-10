package com.astra.call.application.port.in;

import java.util.UUID;

public interface GetIceServersQuery {

    IceServersResponse iceServers(UUID userId);
}
