package com.astra.call.application.port.in;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record IceServersResponse(List<IceServer> iceServers) {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record IceServer(List<String> urls, String username, String credential) {
    }
}
