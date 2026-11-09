package com.astra.user.application.service;

import com.astra.shared.exception.NotFoundException;
import com.astra.shared.exception.UnauthorizedException;
import com.astra.user.application.port.out.LoadUserPort;
import com.astra.user.domain.User;
import java.util.UUID;

final class OwnedUser {

    private OwnedUser() {
    }

    static User actor(LoadUserPort loadUserPort, UUID userId) {
        return loadUserPort.load(userId).orElseThrow(() -> new UnauthorizedException("Not authenticated"));
    }

    static User target(LoadUserPort loadUserPort, UUID userId) {
        return loadUserPort.load(userId).orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }
}
