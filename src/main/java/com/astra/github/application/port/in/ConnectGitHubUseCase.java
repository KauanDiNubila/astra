package com.astra.github.application.port.in;

import java.util.UUID;

public interface ConnectGitHubUseCase {

    UUID connect(String code, String state);
}
