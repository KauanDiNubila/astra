package com.astra.github.application.port.out;

import java.util.UUID;

public interface VerifyConnectStatePort {

    UUID verify(String state);
}
