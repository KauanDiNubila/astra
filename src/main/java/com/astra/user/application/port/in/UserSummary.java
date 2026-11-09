package com.astra.user.application.port.in;

import java.util.UUID;

public record UserSummary(UUID id, String name, String tag, String bio, boolean staff) {

    public String handle() {
        return name + "#" + tag;
    }
}
