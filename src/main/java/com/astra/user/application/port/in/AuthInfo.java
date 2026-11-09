package com.astra.user.application.port.in;

import java.util.List;

public record AuthInfo(String role, List<String> roles, boolean banned) {
}
